package ru.practicum.explore.compilation.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.explore.event.model.Event;

import java.util.Set;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> events;

    @Builder.Default
    @Column(nullable = false)
    private Boolean pinned = false;

    @Column(nullable = false, length = 50)
    private String title;
}
