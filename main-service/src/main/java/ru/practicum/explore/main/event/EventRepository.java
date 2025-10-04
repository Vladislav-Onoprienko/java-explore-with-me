package ru.practicum.explore.main.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explore.main.event.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator " +
            "WHERE e.initiator.id = :initiatorId")
    List<Event> findByInitiatorIdWithCategoryAndInitiator(@Param("initiatorId") Long initiatorId, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator " +
            "WHERE e.id = :eventId AND e.initiator.id = :initiatorId")
    Optional<Event> findByIdAndInitiatorIdWithCategoryAndInitiator(@Param("eventId") Long eventId,
                                                                   @Param("initiatorId") Long initiatorId);

    @Query("SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator " +
            "ORDER BY e.eventDate DESC")
    List<Event> findAllEventsWithCategoryAndInitiator(Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator " +
            "WHERE e.state = 'PUBLISHED' " +
            "ORDER BY e.eventDate DESC")
    List<Event> findPublishedEventsWithCategoryAndInitiator(Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator " +
            "WHERE e.id = :eventId")
    Optional<Event> findByIdWithCategoryAndInitiator(@Param("eventId") Long eventId);

    Boolean existsByCategoryId(Long categoryId);
}
