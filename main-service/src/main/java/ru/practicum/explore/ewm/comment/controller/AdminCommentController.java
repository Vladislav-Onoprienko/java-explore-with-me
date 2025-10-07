package ru.practicum.explore.ewm.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.ewm.comment.dto.CommentDto;
import ru.practicum.explore.ewm.comment.dto.CommentModerationDto;
import ru.practicum.explore.ewm.comment.service.CommentService;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping("/pending")
    public List<CommentDto> getPendingComments(@PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                               @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение комментариев ожидающих модерации");
        Pageable pageable = PageRequest.of(from / size, size);
        return commentService.getPendingComments(pageable);
    }

    @PatchMapping("/{commentId}/moderate")
    public CommentDto moderateComment(@PathVariable Long commentId,
                                      @RequestParam Boolean approved,
                                      @RequestParam(required = false) String reason,
                                      @RequestHeader("X-User-Id") Long moderatorId) {
        log.info("Модерация комментария ID: {}, решение: {}", commentId, approved ? "одобрить" : "отклонить");
        return commentService.moderateComment(moderatorId, commentId, approved, reason);
    }

    @GetMapping("/{commentId}/history")
    public List<CommentModerationDto> getModerationHistory(@PathVariable Long commentId) {
        log.info("Получение истории модерации комментария ID: {}", commentId);
        return commentService.getCommentModerationHistory(commentId);
    }
}
