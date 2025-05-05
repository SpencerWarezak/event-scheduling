package com.example.event_scheduling.repository;

import com.example.event_scheduling.model.Timeslot;
import com.example.event_scheduling.model.User;
import com.example.event_scheduling.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    List<Vote> findByUser(User user);

    List<Vote> findByTimeslot(Timeslot timeslot);

    Vote findByUserAndTimeslot(User user, Timeslot timeslot);

    void deleteByUserAndTimeslot(User user, Timeslot timeslot);

    void deleteByTimeslot(Timeslot timeslot);

    void deleteByUser(User user);
}
