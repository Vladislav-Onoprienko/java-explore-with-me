package ru.practicum.explore.ewm.comment.dto;

import lombok.*;
import ru.practicum.explore.ewm.comment.model.ModerationAction;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentModerationDto {

    private Long id;

    private Long commentId;

    private Long moderatorId;

    private String moderatorName;

    private String reason;

    private ModerationAction action;

    private LocalDateTime createdOn;
}