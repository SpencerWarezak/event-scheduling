package com.example.event_scheduling.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonBackReference
    private User creator;

    private String title;
    private String description;
    private boolean finalized;
    private Integer requiredVotes;

    @ManyToMany(mappedBy = "events")
    @JsonIgnore
    private List<User> users = new ArrayList<User>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Timeslot> timeslots = new ArrayList<Timeslot>();

    public Event() {}
    public Event(User c, String title, String description, Integer requiredVotes) {
        this.creator = c;
        this.title = title;
        this.description = description;
        this.requiredVotes = requiredVotes;
    }

    public void addTimeslot(Timeslot t) {
        if (!this.timeslots.contains(t)) {
            this.timeslots.add(t);
            t.setEvent(this);
        }
    }

    public Timeslot removeTimeslot(Timeslot t) {
        if (!this.timeslots.contains(t)) {
            return null;
        }

        this.timeslots.remove(t);
        t.setEvent(null);
        return t;
    }

    public void addUser(User u) {
        if (!this.users.contains(u)) {
            this.users.add(u);
        }
    }

    public User removeUser(User u) throws Exception {
        if (!this.users.contains(u)) {
            return null;
        }

        if (creator.equals(u)) {
            throw new Exception("Cannot remove the creator from the event, event must be deleted.");
        }
        this.users.remove(u);
        return u;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", finalized=" + finalized +
                '}';
    }
}
