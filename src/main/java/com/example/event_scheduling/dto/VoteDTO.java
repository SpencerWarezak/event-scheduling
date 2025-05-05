package com.example.event_scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteDTO {
    private Long id;
    private Long userId;
    private Long timeslotId;
}
