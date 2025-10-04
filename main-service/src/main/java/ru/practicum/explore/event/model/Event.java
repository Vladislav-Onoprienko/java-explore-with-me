package ru.practicum.explore.event.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.explore.category.model.Category;
import ru.practicum.explore.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000, columnDefinition = "TEXT")
    private String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(length = 7000, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Embedded
    private Location location;

    @Builder.Default
    @Column(nullable = false)
    private Boolean paid = false;

    @Builder.Default
    @Column(nullable = false)
    private Integer participantLimit = 0;

    private LocalDateTime publishedOn;

    @Builder.Default
    @Column(nullable = false)
    private Boolean requestModeration = true;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private EventState state = EventState.PENDING;

    @Column(nullable = false, length = 120)
    private String title;

    @Transient
    @Builder.Default
    private Long confirmedRequests = 0L;

    @Transient
    @Builder.Default
    private Long views = 0L;
}