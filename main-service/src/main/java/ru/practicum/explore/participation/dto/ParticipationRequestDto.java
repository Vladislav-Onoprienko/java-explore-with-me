package ru.practicum.explore.participation.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {

    private LocalDateTime created;

    private Long event;

    private Long id;

    private Long requester;

    private String status;
}