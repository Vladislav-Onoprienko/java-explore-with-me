package ru.practicum.explore.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.dto.EndpointHitDto;
import ru.practicum.explore.dto.ViewStatsDto;
import ru.practicum.explore.server.service.StatsService;

import jakarta.validation.Valid;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        log.info("Сохранение информации о запросе: app={}, uri={}, ip={}",
                endpointHitDto.getApp(), endpointHitDto.getUri(), endpointHitDto.getIp());
        statsService.saveHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique) {

        LocalDateTime startTime = parseDateTime(start);
        LocalDateTime endTime = parseDateTime(end);

        log.info("Получение статистики с {} по {}, uris={}, unique={}",
                startTime, endTime, uris, unique);

        return statsService.getStats(startTime, endTime, uris, unique);
    }

    private LocalDateTime parseDateTime(String encodedDateTime) {
        try {
            String decoded = URLDecoder.decode(encodedDateTime, StandardCharsets.UTF_8);
            return LocalDateTime.parse(decoded, FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException("Неверный формат даты: " + encodedDateTime, e);
        }
    }
}