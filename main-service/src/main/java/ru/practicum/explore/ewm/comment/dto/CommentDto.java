package ru.practicum.explore.ewm.comment.dto;

import lombok.*;
import ru.practicum.explore.ewm.comment.model.CommentStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    private Long id;

    private String text;

    private Long authorId;

    private String authorName;

    private Long eventId;

    private String eventTitle;

    private CommentStatus status;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
}
