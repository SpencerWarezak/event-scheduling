package com.example.event_scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private boolean finalized;
    private Integer requiredVotes;
    private Long creatorId;
    private List<TimeslotDTO> timeslots;
}
