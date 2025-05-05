package com.example.event_scheduling.repository;

import com.example.event_scheduling.model.Event;
import com.example.event_scheduling.model.Timeslot;
import com.example.event_scheduling.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeslotRepository  extends JpaRepository<Timeslot, Long> {

    List<Timeslot> findByCreatorAndEvent(User creator, Event event);

    Timeslot findByEventAndStartTimeAndEndTime(Event event, LocalDateTime startTime, LocalDateTime endTime);

    void deleteByCreatorAndEvent(User creator, Event event);
}
