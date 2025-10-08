package ru.practicum.explore.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.ewm.comment.*;
import ru.practicum.explore.ewm.comment.dto.CommentDto;
import ru.practicum.explore.ewm.comment.dto.CommentModerationDto;
import ru.practicum.explore.ewm.comment.dto.NewCommentDto;
import ru.practicum.explore.ewm.comment.model.Comment;
import ru.practicum.explore.ewm.comment.model.CommentModeration;
import ru.practicum.explore.ewm.comment.model.CommentStatus;
import ru.practicum.explore.ewm.comment.model.ModerationAction;
import ru.practicum.explore.ewm.comment.repository.CommentModerationRepository;
import ru.practicum.explore.ewm.comment.repository.CommentRepository;
import ru.practicum.explore.ewm.event.model.Event;
import ru.practicum.explore.ewm.event.model.EventState;
import ru.practicum.explore.ewm.event.EventRepository;
import ru.practicum.explore.ewm.exception.*;
import ru.practicum.explore.ewm.user.model.User;
import ru.practicum.explore.ewm.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentModerationRepository moderationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, NewCommentDto newCommentDto) {
        User author = getUserById(userId);
        Event event = getEventById(newCommentDto.getEventId());

        if (event.getState() != EventState.PUBLISHED) {
            throw new ValidationException("Нельзя комментировать неопубликованное событие");
        }

        if (commentRepository.existsByAuthorIdAndEventId(userId, event.getId())) {
            throw new ValidationException("Вы уже оставляли комментарий к этому событию");
        }

        Comment comment = commentMapper.toEntity(newCommentDto);
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setStatus(CommentStatus.PENDING);

        Comment savedComment = commentRepository.save(comment);
        log.info("Создан комментарий ID: {} пользователем ID: {} к событию ID: {}",
                savedComment.getId(), userId, event.getId());

        return commentMapper.toDto(savedComment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, String text) {
        Comment comment = getCommentByIdAndAuthorId(commentId, userId);

        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new ValidationException("Нельзя редактировать удаленный комментарий");
        }

        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }
        if (text.length() > 2000) {
            throw new ValidationException("Комментарий не может превышать 2000 символов");
        }

        comment.setText(text.trim());
        comment.setStatus(CommentStatus.PENDING);
        comment.setUpdatedOn(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        log.info("Обновлен комментарий ID: {} пользователем ID: {}", commentId, userId);

        return commentMapper.toDto(updatedComment);
    }

    @Override
    public CommentDto getUserComment(Long userId, Long commentId) {
        Comment comment = getCommentByIdWithUserAndEvent(commentId);

        if (!comment.getAuthor().getId().equals(userId) &&
                comment.getStatus() != CommentStatus.PUBLISHED) {
            throw new CommentAccessDeniedException(
                    "Нет доступа к комментарию ID=" + commentId);
        }

        log.info("Получен комментарий ID: {} пользователем ID: {}", commentId, userId);
        return commentMapper.toDto(comment);
    }

    @Override
    public List<CommentDto> getUserComments(Long userId, Pageable pageable) {
        List<Comment> comments = commentRepository.findByAuthorIdWithUserAndEvent(userId, pageable);
        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getEventComments(Long eventId, Pageable pageable) {
        getEventById(eventId);

        List<Comment> comments = commentRepository.findByEventIdAndStatusWithUserAndEvent(
                eventId, CommentStatus.PUBLISHED, pageable);
        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getPendingComments(Pageable pageable) {
        List<Comment> comments = commentRepository.findByStatusWithUserAndEvent(
                CommentStatus.PENDING, pageable);

        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long getPublishedCommentsCount(Long eventId) {
        getEventById(eventId);
        Long count = commentRepository.countPublishedCommentsByEventId(eventId);
        log.info("Получено количество опубликованных комментариев для события ID: {} - {}", eventId, count);
        return count;
    }

    @Override
    public List<CommentModerationDto> getCommentModerationHistory(Long commentId) {

        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException("Комментарий с ID=" + commentId + " не найден");
        }

        List<CommentModeration> history = moderationRepository.findByCommentIdWithModeratorAndComment(commentId);

        List<CommentModerationDto> result = history.stream()
                .map(commentMapper::toModerationDto)
                .collect(Collectors.toList());

        log.info("Получена история модерации комментария ID: {}, записей: {}", commentId, result.size());
        return result;
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = getCommentByIdAndAuthorId(commentId, userId);

        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new ValidationException("Комментарий уже удален");
        }

        comment.setStatus(CommentStatus.DELETED);
        comment.setUpdatedOn(LocalDateTime.now());

        commentRepository.save(comment);
        log.info("Удален комментарий ID: {} пользователем ID: {}", commentId, userId);
    }

    @Override
    @Transactional
    public CommentDto moderateComment(Long moderatorId, Long commentId, Boolean approved, String reason) {
        User moderator = getUserById(moderatorId);
        Comment comment = getCommentByIdWithUserAndEvent(commentId);

        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ValidationException("Можно модерировать только комментарии со статусом PENDING");
        }

        CommentModeration moderation = CommentModeration.builder()
                .comment(comment)
                .moderator(moderator)
                .reason(reason)
                .action(approved ? ModerationAction.APPROVED : ModerationAction.REJECTED)
                .build();
        moderationRepository.save(moderation);

        comment.setStatus(approved ? CommentStatus.PUBLISHED : CommentStatus.REJECTED);
        comment.setUpdatedOn(LocalDateTime.now());
        Comment updatedComment = commentRepository.save(comment);

        log.info("Комментарий ID: {} {} модератором ID: {}",
                commentId, approved ? "одобрен" : "отклонен", moderatorId);

        return commentMapper.toDto(updatedComment);
    }


    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID=" + userId + " не найден"));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие с ID=" + eventId + " не найдено"));
    }


    private Comment getCommentByIdWithUserAndEvent(Long commentId) {
        return commentRepository.findByIdWithUserAndEvent(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Комментарий с ID=" + commentId + " не найден"));
    }

    private Comment getCommentByIdAndAuthorId(Long commentId, Long authorId) {
        return commentRepository.findByIdAndAuthorId(commentId, authorId)
                .orElseThrow(() -> new CommentAccessDeniedException(
                        "Комментарий с ID=" + commentId + " не найден у пользователя ID=" + authorId));

    }
}