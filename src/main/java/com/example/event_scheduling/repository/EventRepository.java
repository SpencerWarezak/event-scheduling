package com.example.event_scheduling.repository;

import com.example.event_scheduling.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
