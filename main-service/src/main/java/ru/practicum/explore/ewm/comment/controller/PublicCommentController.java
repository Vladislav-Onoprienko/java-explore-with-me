package ru.practicum.explore.ewm.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.ewm.comment.dto.CommentDto;
import ru.practicum.explore.ewm.comment.service.CommentService;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events/{eventId}/comments")
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getEventComments(@PathVariable @Positive Long eventId,
                                             @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                             @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение комментариев события ID: {}", eventId);
        Pageable pageable = PageRequest.of(from / size, size);
        return commentService.getEventComments(eventId, pageable);
    }

    @GetMapping("/count")
    public Long getEventCommentsCount(@PathVariable @Positive Long eventId) {
        log.info("Получение количества комментариев события ID: {}", eventId);
        return commentService.getPublishedCommentsCount(eventId);
    }
}
