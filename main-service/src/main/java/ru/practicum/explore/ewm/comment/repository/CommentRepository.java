package ru.practicum.explore.ewm.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.ewm.comment.model.CommentStatus;
import ru.practicum.explore.ewm.comment.model.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.author JOIN FETCH c.event " +
            "WHERE c.event.id = :eventId AND c.status = :status ORDER BY c.createdOn DESC")
    List<Comment> findByEventIdAndStatusWithUserAndEvent(@Param("eventId") Long eventId,
                                                         @Param("status") CommentStatus status,
                                                         Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN FETCH c.author JOIN FETCH c.event " +
            "WHERE c.author.id = :authorId ORDER BY c.createdOn DESC")
    List<Comment> findByAuthorIdWithUserAndEvent(@Param("authorId") Long authorId, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN FETCH c.author JOIN FETCH c.event " +
            "WHERE c.status = :status ORDER BY c.createdOn DESC")
    List<Comment> findByStatusWithUserAndEvent(@Param("status") CommentStatus status, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN FETCH c.author JOIN FETCH c.event WHERE c.id = :id")
    Optional<Comment> findByIdWithUserAndEvent(@Param("id") Long id);

    Optional<Comment> findByIdAndAuthorId(Long id, Long authorId);

    boolean existsByAuthorIdAndEventId(Long authorId, Long eventId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.event.id = :eventId AND c.status = 'PUBLISHED'")
    Long countPublishedCommentsByEventId(@Param("eventId") Long eventId);
}
