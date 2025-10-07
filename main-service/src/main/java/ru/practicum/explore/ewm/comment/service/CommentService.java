package ru.practicum.explore.ewm.comment.service;

import ru.practicum.explore.ewm.comment.dto.CommentDto;
import ru.practicum.explore.ewm.comment.dto.CommentModerationDto;
import ru.practicum.explore.ewm.comment.dto.NewCommentDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long userId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long commentId, String text);

    CommentDto getUserComment(Long userId, Long commentId);

    List<CommentDto> getUserComments(Long userId, Pageable pageable);

    List<CommentDto> getEventComments(Long eventId, Pageable pageable);

    List<CommentDto> getPendingComments(Pageable pageable);

    Long getPublishedCommentsCount(Long eventId);

    List<CommentModerationDto> getCommentModerationHistory(Long commentId);

    void deleteComment(Long userId, Long commentId);

    CommentDto moderateComment(Long moderatorId, Long commentId, Boolean approved, String reason);
}
