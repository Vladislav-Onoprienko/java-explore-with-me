package ru.practicum.explore.ewm.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explore.ewm.comment.model.CommentModeration;

import java.util.List;

public interface CommentModerationRepository extends JpaRepository<CommentModeration, Long> {

    @Query("SELECT cm FROM CommentModeration cm JOIN FETCH cm.moderator JOIN FETCH cm.comment " +
            "WHERE cm.comment.id = :commentId ORDER BY cm.createdOn DESC")
    List<CommentModeration> findByCommentIdWithModeratorAndComment(@Param("commentId") Long commentId);
}