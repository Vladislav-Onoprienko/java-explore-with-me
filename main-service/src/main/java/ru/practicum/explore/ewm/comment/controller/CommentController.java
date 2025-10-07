package ru.practicum.explore.ewm.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.ewm.comment.dto.CommentDto;
import ru.practicum.explore.ewm.comment.dto.NewCommentDto;
import ru.practicum.explore.ewm.comment.service.CommentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable Long userId,
                                    @Valid @RequestBody NewCommentDto newCommentDto) {
        log.info("Создание комментария пользователем ID: {} к событию ID: {}",
                userId, newCommentDto.getEventId());
        return commentService.createComment(userId, newCommentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable Long userId,
                                    @PathVariable Long commentId,
                                    @RequestParam String text) {
        log.info("Обновление комментария ID: {} пользователем ID: {}", commentId, userId);
        return commentService.updateComment(userId, commentId, text);
    }

    @GetMapping("/{commentId}")
    public CommentDto getComment(@PathVariable Long userId,
                                 @PathVariable Long commentId) {
        log.info("Получение комментария ID: {} пользователем ID: {}", commentId, userId);
        return commentService.getUserComment(userId, commentId);
    }

    @GetMapping
    public List<CommentDto> getUserComments(@PathVariable Long userId,
                                            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                            @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение комментариев пользователя ID: {}", userId);
        Pageable pageable = PageRequest.of(from / size, size);
        return commentService.getUserComments(userId, pageable);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        log.info("Удаление комментария ID: {} пользователем ID: {}", commentId, userId);
        commentService.deleteComment(userId, commentId);
    }
}
