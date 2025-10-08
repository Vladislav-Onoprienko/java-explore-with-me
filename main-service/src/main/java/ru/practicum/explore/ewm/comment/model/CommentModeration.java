package ru.practicum.explore.ewm.comment.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import ru.practicum.explore.ewm.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_moderations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentModeration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id", nullable = false)
    private User moderator;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ModerationAction action;

    @CreationTimestamp
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;
}
