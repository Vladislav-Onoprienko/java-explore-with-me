package ru.practicum.explore.main.compilation.dto;

import lombok.*;
import ru.practicum.explore.main.event.dto.response.EventShortDto;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {

    private Set<EventShortDto> events;

    private Long id;

    private Boolean pinned;

    private String title;
}
