package com.example.event_scheduling;

import com.example.event_scheduling.controller.EventController;
import com.example.event_scheduling.dto.CreateEventRequest;
import com.example.event_scheduling.dto.EventDTO;
import com.example.event_scheduling.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
    }

    @Test
    void testGetEvents_Success() throws Exception {
        List<EventDTO> events = List.of(new EventDTO(1L, "Event 1", "Description", false, 5, 1L, new ArrayList<>()));
        when(eventService.getEvents(anyLong())).thenReturn(events);

        mockMvc.perform(get("/events/getEvents")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data[0].title").value("Event 1"));
    }

    @Test
    void testGetEvents_UserNotFound() throws Exception {
        when(eventService.getEvents(anyLong())).thenReturn(null);

        mockMvc.perform(get("/events/getEvents")
                        .param("userId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void testCreateEvent_Success() throws Exception {
        CreateEventRequest request = new CreateEventRequest(1L, "Event Title", "Event Description", 5, "2025-05-05T00:00:00", "2025-05-06T00:00:00");
        EventDTO eventDTO = new EventDTO(1L, "Event Title", "Description", false, 5, 1L, new ArrayList<>());
        when(eventService.createEvent(eq(1L), eq("Event Title"), eq("Event Description"), any(), any(), eq(5)))
                .thenReturn(eventDTO);

        mockMvc.perform(post("/events/create")
                        .contentType("application/json")
                        .content("{\"creatorId\": 1, \"title\": \"Event Title\", \"description\": \"Event Description\", \"startDate\": \"2025-05-05T00:00:00\", \"endDate\": \"2025-05-06T00:00:00\", \"requiredVotes\": 5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.title").value("Event Title"));
    }

    @Test
    void testCreateEvent_Failure() throws Exception {
        CreateEventRequest request = new CreateEventRequest(1L, "Event Title", "Event Description", 5, "2025-05-05T00:00:00", "2025-05-06T00:00:00");
        when(eventService.createEvent(eq(1L), eq("Event Title"), eq("Event Description"), any(), any(), eq(5)))
                .thenThrow(new RuntimeException("Event creation failed"));

        mockMvc.perform(post("/events/create")
                        .contentType("application/json")
                        .content("{\"creatorId\": 1, \"title\": \"Event Title\", \"description\": \"Event Description\", \"startDate\": \"2025-05-05T00:00:00\", \"endDate\": \"2025-05-06T00:00:00\", \"requiredVotes\": 5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("java.lang.RuntimeException: Event creation failed"));
    }

    @Test
    void testInvite_Success() throws Exception {
        EventDTO eventDTO = new EventDTO(1L, "Event Title", "Description", false, 5, 1L, new ArrayList<>());
        when(eventService.inviteToEvent(anyLong(), anyLong(), anyLong())).thenReturn(eventDTO);

        mockMvc.perform(post("/events/invite")
                        .param("senderId", "1")
                        .param("eventId", "1")
                        .param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.title").value("Event Title"));
    }

    @Test
    void testInvite_Failure() throws Exception {
        when(eventService.inviteToEvent(anyLong(), anyLong(), anyLong())).thenThrow(new RuntimeException("Failed to invite user to event"));

        mockMvc.perform(post("/events/invite")
                        .param("senderId", "1")
                        .param("eventId", "1")
                        .param("userId", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("java.lang.RuntimeException: Failed to invite user to event"));
    }

    @Test
    void testFinalizeEvent_Success() throws Exception {
        EventDTO finalizedEvent = new EventDTO(1L, "Event Title", "Description", false, 5, 1L, new ArrayList<>());
        when(eventService.finalizeEvent(anyLong(), anyLong(), anyBoolean())).thenReturn(finalizedEvent);

        mockMvc.perform(post("/events/finalizeEvent")
                        .param("userId", "1")
                        .param("eventId", "1")
                        .param("force", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.title").value("Event Title"));
    }

    @Test
    void testFinalizeEvent_Failure() throws Exception {
        when(eventService.finalizeEvent(anyLong(), anyLong(), anyBoolean())).thenThrow(new RuntimeException("Unable to finalize event"));

        mockMvc.perform(post("/events/finalizeEvent")
                        .param("userId", "1")
                        .param("eventId", "1")
                        .param("force", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("java.lang.RuntimeException: Unable to finalize event"));
    }
}

