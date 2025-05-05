package com.example.event_scheduling.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "timeslots")
public class Timeslot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @OneToMany(mappedBy = "timeslot", cascade = CascadeType.ALL)
    private List<Vote> votes = new ArrayList<Vote>();

    private boolean finalized = false;

    public Timeslot() {}
    public Timeslot(Event e, User c, LocalDateTime start, LocalDateTime end) {
        this.event = e;
        this.creator = c;
        this.startTime = start;
        this.endTime = end;
    }

    public void addVote(Vote v) {
        if (!this.votes.contains(v)) {
            this.votes.add(v);
            v.setTimeslot(this);
        }
    }

    public Vote removeVote(Vote v) {
        if (!this.votes.contains(v)) {
            return null;
        }

        this.votes.remove(v);
        v.setTimeslot(null);
        return v;
    }

    @Override
    public String toString() {
        return "Timeslot{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", finalized=" + finalized +
                '}';
    }
}
