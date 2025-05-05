package com.example.event_scheduling.controller;

import com.example.event_scheduling.dto.ApiResponse;
import com.example.event_scheduling.dto.CreateEventRequest;
import com.example.event_scheduling.dto.EventDTO;
import com.example.event_scheduling.dto.TimeslotDTO;
import com.example.event_scheduling.service.EventService;
import com.example.event_scheduling.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final Logger logger = LoggerFactory.getLogger(EventController.class);

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/getEvents")
    public ResponseEntity<ApiResponse<List<EventDTO>>> getEvents(@RequestParam Long userId) {
        logger.info("Retrieving events for user {}", userId);
        ApiResponse<List<EventDTO>> response = new ApiResponse<List<EventDTO>>();
        List<EventDTO> events = eventService.getEvents(userId);

        if (events == null) {
            response.message = "User not found";
            return ResponseEntity.badRequest().body(response);
        }

        response.message = "Success";
        response.data = events;
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<EventDTO>> createEvent(@RequestBody CreateEventRequest request) {
        logger.info("Creating event...");
        ApiResponse<EventDTO> response = new ApiResponse<EventDTO>();
        try {
            EventDTO newEvent = eventService.createEvent(
                    request.getCreatorId(),
                    request.getTitle(),
                    request.getDescription(),
                    Utils.getUTCDate(request.getStartDate()),
                    Utils.getUTCDate(request.getEndDate()),
                    request.getRequiredVotes()
            );

            if (newEvent == null) {
                throw new Exception("Event creation failed");
            }

            response.message = "Success";
            response.data = newEvent;
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Exception encountered in event creation: {}", e.toString());
            response.message = e.toString();
            response.data = null;
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<EventDTO>> invite(@RequestParam Long senderId,
                                                               @RequestParam Long eventId,
                                                               @RequestParam Long userId) {
        logger.info("Inviting user to event...");
        ApiResponse<EventDTO> response = new ApiResponse<EventDTO>();

        try {
            EventDTO eventResponse = eventService.inviteToEvent(senderId, eventId, userId);
            if (eventResponse == null) {
                throw new Exception("Failed to invite user to event");
            }

            response.message = "Success";
            response.data = eventResponse;
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Exception encountered in event invitation: {}", e.toString());
            response.message = e.toString();
            response.data = null;
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/decline")
    public ResponseEntity<ApiResponse<String>> decline(@RequestParam Long eventId,
                                                             @RequestParam Long userId) {
        logger.info("Declining event invite request...");
        ApiResponse<String> response = new ApiResponse<String>();

        try {
            String eventResponse = eventService.declineEvent(eventId, userId);
            if (eventResponse == null) {
                throw new Exception("Failed to decline event invitation");
            }

            response.message = "Success";
            response.data = eventResponse;
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Exception encountered in declining event invitation: {}", e.toString());
            response.message = e.toString();
            response.data = null;
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/propose")
    public ResponseEntity<ApiResponse<EventDTO>> propose(@RequestParam Long eventId,
                                                         @RequestParam Long userId,
                                                         @RequestParam String startTime,
                                                         @RequestParam String endTime) {
        logger.info("User {} proposing timeslot {} to {} for event {}", userId, startTime, endTime, eventId);
        ApiResponse<EventDTO> response = new ApiResponse<EventDTO>();

        try {
            boolean canEdit = eventService.checkEventFinalized(eventId);
            if (!canEdit) throw new Exception("Event is already finalized!");

            EventDTO eventResponse = eventService.proposeTimeslot(eventId,
                                                                  userId,
                                                                  Utils.getUTCDate(startTime),
                                                                  Utils.getUTCDate(endTime));
            if (eventResponse == null) {
                throw new Exception("Failed to propose timeslot");
            }

            response.message = "Success";
            response.data = eventResponse;
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Exception encountered in timeslot proposal: {}", e.toString());
            response.message = e.toString();
            response.data = null;
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/vote")
    public ResponseEntity<ApiResponse<EventDTO>> vote(@RequestParam Long userId,
                                                                   @RequestParam Long eventId,
                                                                   @RequestParam Long timeslotId,
                                                                   @RequestParam(required = false) Boolean remove) {
        logger.info("User {} voting for timeslot {} on event {}", userId, timeslotId, eventId);
        ApiResponse<EventDTO> response = new ApiResponse<EventDTO>();

        try {
            boolean canEdit = eventService.checkEventFinalized(eventId);
            if (!canEdit) throw new Exception("Event is already finalized!");

            boolean shouldRemove = Boolean.TRUE.equals(remove);
            EventDTO eventResponse = eventService.vote(userId, eventId, timeslotId, shouldRemove);
            if (eventResponse == null) {
                throw new Exception("Failed to cast vote for timeslot");
            }

            response.message = "Success";
            response.data = eventResponse;
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Exception encountered in voting: {}", e.toString());
            response.message = e.toString();
            response.data = null;
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/getVotes")
    public ResponseEntity<ApiResponse<List<TimeslotDTO>>> getVotes(@RequestParam Long userId,
                                                                   @RequestParam Long eventId,
                                                                   @RequestParam(required = false) Long timeslotId) {
        logger.info("User {} attempting to view {} vote counts for event {}", userId, timeslotId == null ? "all" : 1, eventId);
        ApiResponse<List<TimeslotDTO>> response = new ApiResponse<List<TimeslotDTO>>();

        try {
            List<TimeslotDTO> timeslotVotes = eventService.getVotes(userId, eventId, timeslotId);
            if (timeslotVotes == null) {
                throw new Exception("Unable to retrieve votes.");
            }

            response.message = "Success";
            response.data = timeslotVotes;
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Exception encountered in viewing votes: {}", e.toString());
            response.message = e.toString();
            response.data = null;
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/finalizeEvent")
    public ResponseEntity<ApiResponse<EventDTO>> finalizeEvent(@RequestParam Long userId,
                                                               @RequestParam Long eventId,
                                                               @RequestParam(required = false) Boolean force) {
        logger.info("User {} finalizing event {}", userId, eventId);
        ApiResponse<EventDTO> response = new ApiResponse<EventDTO>();

        try {
            Boolean forceFinalization = Boolean.TRUE.equals(force);
            EventDTO finalizedEvent = eventService.finalizeEvent(userId, eventId, forceFinalization);

            if (finalizedEvent == null) {
                throw new Exception("Unable to retrieve votes.");
            }

            response.message = "Success";
            response.data = finalizedEvent;
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Exception encountered in finalizing event: {}", e.toString());
            response.message = e.toString();
            response.data = null;
            return ResponseEntity.badRequest().body(response);
        }
    }
}
