package ru.practicum.explore.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.explore.dto.EndpointHitDto;
import ru.practicum.explore.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;

    @Value("${stats.server.url:http://localhost:9090}")
    private final String serverUrl;

    public void saveHit(String app, String uri, String ip) {
        EndpointHitDto hit = EndpointHitDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();

        try {
            restTemplate.postForEntity(serverUrl + "/hit", hit, Void.class);
            log.debug("Статистика сохранена: app={}, uri={}, ip={}", app, uri, ip);

        } catch (ResourceAccessException e) {
            log.warn("Сервис статистики недоступен: {}", e.getMessage());

        } catch (HttpStatusCodeException e) {
            log.warn("Ошибка HTTP {} при сохранении статистики: {}",
                    e.getStatusCode(), e.getMessage());

        } catch (RestClientException e) {
            log.warn("Ошибка при сохранении статистики: {}", e.getMessage());
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, boolean unique) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(serverUrl + "/stats")
                    .queryParam("start", formatDateTime(start))
                    .queryParam("end", formatDateTime(end))
                    .queryParam("unique", unique);

            if (uris != null && !uris.isEmpty()) {
                builder.queryParam("uris", String.join(",", uris));
            }

            ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(
                    builder.toUriString(), ViewStatsDto[].class);

            return response.getBody() != null ? List.of(response.getBody()) : List.of();

        } catch (RestClientException e) {
            log.warn("Не удалось получить статистику: {}", e.getMessage());
            return List.of();
        }
    }

    public Map<String, Long> getUriHits(LocalDateTime start, LocalDateTime end, List<String> uris) {
        List<ViewStatsDto> stats = getStats(start, end, uris, false);
        return stats.stream()
                .collect(Collectors.toMap(
                        ViewStatsDto::getUri,
                        dto -> dto.getHits() != null ? dto.getHits() : 0L
                ));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }
}