package ru.practicum.main_server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.main_server.model.Event;
import ru.practicum.main_server.model.State;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT e FROM Event AS e " +
            "WHERE ((:text) IS NULL " +
            "OR UPPER(e.annotation) LIKE UPPER(CONCAT('%', :text, '%')) " +
            "OR UPPER(e.description) LIKE UPPER(CONCAT('%', :text, '%'))) " +
            "AND ((:categories) IS NULL OR e.category.id IN :categories) " +
            "AND ((:paid) IS NULL OR e.paid = :paid) " +
            "AND (e.eventDate >= :start) " +
            "AND (e.eventDate <= :end) " +
            "AND ((e.confirmedReq) < (e.participantLimit)) " +
            "ORDER BY (e.eventDate) ASC")
    List<Event> searchEventsAvailableOrderByEventDate(String text, List<Long> categories, Boolean paid, LocalDateTime start,
                                                      LocalDateTime end, PageRequest pageRequest);

    @Query("SELECT e FROM Event AS e " +
            "WHERE ((:text) IS NULL " +
            "OR UPPER(e.annotation) LIKE UPPER(CONCAT('%', :text, '%')) " +
            "OR UPPER(e.description) LIKE UPPER(CONCAT('%', :text, '%'))) " +
            "AND ((:categories) IS NULL OR e.category.id IN :categories) " +
            "AND ((:paid) IS NULL OR e.paid = :paid) " +
            "AND (e.eventDate >= :start) " +
            "AND (e.eventDate <= :end) " +
            "AND ((e.confirmedReq) < (e.participantLimit)) " +
            "ORDER BY (e.views) ASC")
    List<Event> searchEventsAvailableOrderByViews(String text, List<Long> categories, Boolean paid, LocalDateTime start,
                                                  LocalDateTime end, PageRequest pageRequest);

    @Query("SELECT e FROM Event AS e " +
            "WHERE ((:text) IS NULL " +
            "OR UPPER(e.annotation) LIKE UPPER(CONCAT('%', :text, '%')) " +
            "OR UPPER(e.description) LIKE UPPER(CONCAT('%', :text, '%'))) " +
            "AND ((:categories) IS NULL OR e.category.id IN :categories) " +
            "AND ((:paid) IS NULL OR e.paid = :paid) " +
            "AND (e.eventDate >= :start) " +
            "AND (e.eventDate <= :end) " +
            "ORDER BY (e.eventDate) ASC")
    List<Event> searchEventsAllOrderByEventDate(String text, List<Long> categories, Boolean paid, LocalDateTime start,
                                                LocalDateTime end, PageRequest pageRequest);

    @Query("SELECT e FROM Event AS e " +
            "WHERE ((:text) IS NULL " +
            "OR UPPER(e.annotation) LIKE UPPER(CONCAT('%', :text, '%')) " +
            "OR UPPER(e.description) LIKE UPPER(CONCAT('%', :text, '%'))) " +
            "AND ((:categories) IS NULL OR e.category.id IN :categories) " +
            "AND ((:paid) IS NULL OR e.paid = :paid) " +
            "AND (e.eventDate >= :start) " +
            "AND (e.eventDate <= :end) " +
            "ORDER BY (e.views) ASC")
    List<Event> searchEventsAllOrderByViews(String text, List<Long> categories, Boolean paid, LocalDateTime start,
                                            LocalDateTime end, PageRequest pageRequest);

    Page<Event> findAllByInitiatorId(Long userId, PageRequest pageRequest);

    @Query("SELECT e FROM Event AS e " +
            "WHERE ((:users) IS NULL OR e.initiator.id IN :users) " +
            "AND ((:states) IS NULL OR e.state IN :states) " +
            "AND ((:categories) IS NULL OR e.category.id IN :categories) " +
            "AND (e.eventDate >= :start) " +
            "AND (e.eventDate <= :end)")
    List<Event> searchEventsByAdmin(List<Long> users, List<State> states, List<Long> categories,
                                    LocalDateTime start, LocalDateTime end, PageRequest pageRequest);

}

