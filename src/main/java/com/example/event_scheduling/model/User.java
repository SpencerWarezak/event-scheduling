package com.example.event_scheduling.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String firstName;
    private String lastName;

    @JsonIgnore
    private String password;

    @ManyToMany
    @JoinTable(
            name = "user_event",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    @JsonManagedReference
    private List<Event> events = new ArrayList<Event>();

    public User() {}

    public void addEvent(Event e) {
        if (!this.events.contains(e)) {
            this.events.add(e);
            e.addUser(this);
        }
    }

    public Event removeEvent(Event e) throws Exception {
        if (!this.events.contains(e)) {
            return null;
        }

        this.events.remove(e);
        e.removeUser(this);
        return e;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
