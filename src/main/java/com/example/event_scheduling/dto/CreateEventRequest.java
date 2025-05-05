package com.example.event_scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {
    private Long creatorId;
    private String title;
    private String description;
    private Integer requiredVotes;
    private String startDate;
    private String endDate;
}
