package ru.practicum.explore.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewStatsDto {
    private String app;
    private String uri;
    private Long hits;
}



