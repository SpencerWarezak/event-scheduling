package com.example.event_scheduling.service;

import com.example.event_scheduling.dto.EventDTO;
import com.example.event_scheduling.dto.TimeslotDTO;
import com.example.event_scheduling.dto.VoteDTO;
import com.example.event_scheduling.model.Event;
import com.example.event_scheduling.model.Timeslot;
import com.example.event_scheduling.model.User;
import com.example.event_scheduling.model.Vote;
import com.example.event_scheduling.repository.EventRepository;
import com.example.event_scheduling.repository.TimeslotRepository;
import com.example.event_scheduling.repository.UserRepository;
import com.example.event_scheduling.repository.VoteRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TimeslotRepository timeslotRepository;
    private final VoteRepository voteRepository;
    private final Logger logger = LoggerFactory.getLogger(EventService.class);
    private final Integer defaultRequiredVotes = 5;

    @Autowired
    public EventService(UserRepository userRepository,
                        EventRepository eventRepository,
                        TimeslotRepository timeslotRepository,
                        VoteRepository voteRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.timeslotRepository = timeslotRepository;
        this.voteRepository = voteRepository;
    }

    public List<EventDTO> getEvents(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            logger.info("No user found for userId {}", userId);
            return null;
        }

        logger.info("Found {} events for user {}", user.getEvents().size(), userId);
        return user.getEvents().stream()
                .map(event -> toEventDTO(event, userId))
                .collect(Collectors.toList());
    }

    @Transactional
    public EventDTO createEvent(Long creatorId,
                                String title,
                                String description,
                                LocalDateTime startDate,
                                LocalDateTime endDate,
                                Integer requiredVotes) {
        logger.info("Creating new event for {} from {} to {}", creatorId, startDate, endDate);

        User user = userRepository.findById(creatorId).orElse(null);
        if (user == null) {
            logger.error("No user found for creatorId {}", creatorId);
            return null;
        }

        if (!isValidDateRange(startDate, endDate)) return null;

        try {
            Event newEvent = new Event(user,
                    title != null ? title : "Untitled Event",
                    description != null ? description : "No description provided.",
                    requiredVotes != null ? requiredVotes : defaultRequiredVotes);

            Timeslot initialTimeslot = new Timeslot(newEvent, user, startDate, endDate);
            newEvent.addTimeslot(initialTimeslot);
            user.addEvent(newEvent);

            logger.info("Saving new Event {}", newEvent);
            logger.info("Saving new User {}", user);
            logger.info("Saving new Timeslot {}", initialTimeslot);

            eventRepository.save(newEvent);
            userRepository.save(user);

            return toEventDTO(newEvent, creatorId);
        } catch (Exception e) {
            logger.error("Exception encountered in createEvent: {}", e.toString());
            return null;
        }
    }

    @Transactional
    public EventDTO inviteToEvent(Long senderId,
                                  Long eventId,
                                  Long userId) {
        logger.info("Inviting user {} to event {}", userId, eventId);
        User sender = userRepository.findById(senderId).orElse(null);
        User invitee = userRepository.findById(userId).orElse(null);
        Event event = eventRepository.findById(eventId).orElse(null);

        if (sender == null || invitee == null || event == null) {
            logger.info("Invalid senderId ({})/userId ({})/eventId ({}), please check values.", senderId, userId, eventId);
            return null;
        }

        if (senderId.longValue() != event.getCreator().getId().longValue()) {
            logger.warn("Only event creators are allowed to invite users to an event.");
            return null;
        }

        if (!event.getUsers().contains(invitee)) {
            event.addUser(invitee);
            invitee.addEvent(event);

            userRepository.save(invitee);
            eventRepository.save(event);
        }
        return toEventDTO(event, senderId);
    }

    @Transactional
    public String declineEvent(Long eventId, Long userId) {
        logger.info("User {} declining event invitation {}", userId, eventId);
        User user = userRepository.findById(userId).orElse(null);
        Event event = eventRepository.findById(eventId).orElse(null);
        List<Timeslot> timeslots = timeslotRepository.findByCreatorAndEvent(user, event);

        if (user == null || event == null) {
            logger.info("Invalid userId ({})/eventId ({}), please check values.", userId, eventId);
            return null;
        }

        try {
            if (timeslots != null) {
                // remove all votes a user has cast if they are declining after having accepted
                for (Timeslot t : timeslots) {
                    logger.info("Removing timeslot {}", t.getId());
                    voteRepository.deleteByUserAndTimeslot(user, t);
                }
                // remove all timeslots a user has proposed if they are declining after having accepted
                timeslotRepository.deleteByCreatorAndEvent(user, event);
            }

            event.removeUser(user);
            user.removeEvent(event);

            userRepository.save(user);
            eventRepository.save(event);
            return "Successfully declined event";
        } catch (Exception e) {
            logger.info("Exception encountered in declineEvent: {}", e.toString());
            return null;
        }
    }

    @Transactional
    public EventDTO proposeTimeslot(Long eventId,
                                    Long userId,
                                    LocalDateTime start,
                                    LocalDateTime end) {
        logger.info("User {} proposing event {} timeslot {} to {}", userId, eventId, start.toString(), end.toString());
        User user = userRepository.findById(userId).orElse(null);
        Event event = eventRepository.findById(eventId).orElse(null);

        if (user == null || event == null) {
            logger.info("Invalid userId ({})/eventId ({}), please check values.", userId, eventId);
            return null;
        }

        if (!event.getUsers().contains(user)) {
            logger.warn("Unaffiliated user trying to propose timeslot, rejecting request");
            return null;
        }

        if (!isValidDateRange(start, end)) return null;

        try {
            Timeslot t = new Timeslot(event, user, start, end);
            if (timeslotRepository.findByEventAndStartTimeAndEndTime(event, start, end) != null) {
                logger.info("The range {} to {} has already been proposed for event {}", start, end, eventId);
                return null;
            }
            event.addTimeslot(t);
            eventRepository.save(event);

            return toEventDTO(event, userId);
        } catch (Exception e) {
            logger.error("Exception encountered in proposeTimeslot: {}", e.toString());
            return null;
        }
    }

    @Transactional
    public EventDTO vote(Long userId,
                         Long eventId,
                         Long timeslotId,
                         Boolean remove) {
        logger.info("User {} {} vote for timeslot {} on event {}", userId, remove ? "removing" : "casting", timeslotId, eventId);
        User user = userRepository.findById(userId).orElse(null);
        Event event = eventRepository.findById(eventId).orElse(null);
        Timeslot timeslot = timeslotRepository.findById(timeslotId).orElse(null);

        if (user == null || event == null || timeslot == null) {
            logger.info("Invalid userId ({})/eventId ({})/timeslotId ({}), please check values.", userId, eventId, timeslotId);
            return null;
        }

        if (user.getId().longValue() == event.getCreator().getId().longValue()) {
            logger.info("Event creator is not allowed to vote on timeslots");
            return null;
        }

        if (!event.getUsers().contains(user)) {
            logger.info("Only invited participants are allowed to vote");
            return null;
        }

        return remove ? removeVoteFromTimeslot(user, event, timeslot) : addVoteToTimeslot(user, event, timeslot);
    }

    private EventDTO addVoteToTimeslot(User user,
                             Event event,
                             Timeslot timeslot) {
        Vote newVote = new Vote();
        newVote.setTimeslot(timeslot);
        newVote.setUser(user);

        voteRepository.save(newVote);

        Timeslot updatedT = event.removeTimeslot(timeslot);
        updatedT.addVote(newVote);
        event.addTimeslot(updatedT);

        eventRepository.save(event);
        return toEventDTO(event, user.getId());
    }

    private EventDTO removeVoteFromTimeslot(User user,
                                            Event event,
                                            Timeslot timeslot) {
        Vote v = voteRepository.findByUserAndTimeslot(user, timeslot);
        if (v == null) {
            logger.warn("No votes found from user {} for timeslot {}", user.getId(), timeslot.getId());
            return null;
        }

        Timeslot updatedT = event.removeTimeslot(timeslot);
        timeslot.removeVote(v);
        event.addTimeslot(updatedT);
        eventRepository.save(event);

        return toEventDTO(event, user.getId());
    }

    public List<TimeslotDTO> getVotes(Long userId, Long eventId, Long timeslotId) {
        logger.info("User {} attempting to view votes for event {} with timeslots {}", userId, eventId, timeslotId == null ? "all" : timeslotId);
        User user = userRepository.findById(userId).orElse(null);
        Event event = eventRepository.findById(eventId).orElse(null);
        List<Timeslot> timeslots;

        if (user == null || event == null) {
            logger.error("Invalid userId ({})/eventId ({}), please check values.", userId, eventId);
            return null;
        }

        if (user.getId().longValue() != event.getCreator().getId().longValue()) {
            logger.warn("User authorized to view votes.");
            return null;
        }

        if (timeslotId == null) {
            logger.info("Retrieving votes for all timeslots with eventId {}", eventId);
            timeslots = event.getTimeslots();
        } else {
            logger.info("Retrieving only for timeslotId {}", timeslotId);
            Timeslot retrievedT = timeslotRepository.findById(timeslotId).orElse(null);

            if (retrievedT == null) {
                logger.error("Unable to retrieve timeslot for timeslotId {}", timeslotId);
                return null;
            }

            timeslots = List.of(retrievedT);
        }

        return timeslots.stream()
                .map(timeslot -> toTimeslotDTO(timeslot, true))
                .collect(Collectors.toList());
    }

    @Transactional
    public EventDTO finalizeEvent(Long userId,
                                  Long eventId,
                                  Boolean force) {
        logger.info("User {} trying to finalize event {} with force set to {}", userId, eventId, force);
        User user = userRepository.findById(userId).orElse(null);
        Event event = eventRepository.findById(eventId).orElse(null);

        if (user == null || event == null) {
            logger.error("Invalid userId ({})/eventId ({}), please check values.", userId, eventId);
            return null;
        }

        if (user.getId().longValue() != event.getCreator().getId().longValue()) {
            logger.error("User unauthorized for event finalization. Only the creator can finalize an event.");
            return null;
        }

        Timeslot maxTimeslot = event.getTimeslots()
                .stream()
                .max(Comparator.comparingInt(timeslot -> timeslot.getVotes().size()))
                .orElse(null);

        if (maxTimeslot == null) {
            logger.warn("No max timeslot could be retrieved");
            return null;
        }

        if (!force && maxTimeslot.getVotes().size() < event.getRequiredVotes()) {
            logger.warn("A maximum number of votes has not yet been reached.");
            return null;
        }

        return finalizeEventHelper(user, event, maxTimeslot);
    }

    private boolean isValidDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            logger.error("Invalid startDate ({})/endDate ({})", startDate, endDate);
            return false;
        }

        if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
            logger.error("Start date must be before end date");
            return false;
        }

        return true;
    }

    private EventDTO finalizeEventHelper(User user, Event event, Timeslot finalTimeslot) {
        // check if timeslot is valid
        LocalDateTime currentUTC = LocalDateTime.now(ZoneOffset.UTC);
        if (finalTimeslot.getStartTime().isBefore(currentUTC)) {
            logger.error("Timeslot is no longer valid! {} gte {}", currentUTC, finalTimeslot.getStartTime());
            return null;
        }

        event.setFinalized(true);
        Timeslot updatedT = event.removeTimeslot(finalTimeslot);
        updatedT.setFinalized(true);
        event.addTimeslot(updatedT);
        eventRepository.save(event);

        return toEventDTO(event, user.getId());
    }

    public Boolean checkEventFinalized(Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);

        if (event == null) {
            logger.error("Event {} does not exist.", eventId);
            return false;
        }

        if (event.isFinalized()) {
            logger.info("Cannot operate on eevent {}, it is already finalized", eventId);
            return false;
        }

        logger.info("Event is not yet finalized, okay to continue with operations.");
        return true;
    }

    private EventDTO toEventDTO(Event event, Long userId) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setFinalized(event.isFinalized());
        dto.setCreatorId(event.getCreator().getId());

        boolean isCreator = event.getCreator().getId().longValue() == userId.longValue();

        dto.setTimeslots(event.getTimeslots().stream()
                .map(timeslot -> toTimeslotDTO(timeslot, isCreator))
                .collect(Collectors.toList()));

        return dto;
    }

    private TimeslotDTO toTimeslotDTO(Timeslot timeslot, boolean isCreator) {
        TimeslotDTO dto = new TimeslotDTO();
        dto.setId(timeslot.getId());
        dto.setStartTime(timeslot.getStartTime());
        dto.setEndTime(timeslot.getEndTime());
        dto.setFinalized(timeslot.isFinalized());
        dto.setCreatorId(timeslot.getCreator().getId());

        if (isCreator) {
            dto.setVotes(timeslot.getVotes().stream()
                    .map(this::toVoteDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private VoteDTO toVoteDTO(Vote vote) {
        VoteDTO dto = new VoteDTO();
        dto.setId(vote.getId());
        dto.setUserId(vote.getUser().getId());
        dto.setTimeslotId(vote.getTimeslot().getId());
        return dto;
    }
}
