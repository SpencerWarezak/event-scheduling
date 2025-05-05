package com.example.event_scheduling.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "votes")
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "timeslot_id", nullable = false)
    private Timeslot timeslot;

    @Override
    public String toString() {
        return "Vote{" +
                "id=" + id +
                ", user=" + user.getId() +
                ", timeslot=" + timeslot.getId() +
                '}';
    }

    public Vote() {}
}
