package ru.practicum.explore.main.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explore.client.StatsClient;
import ru.practicum.explore.dto.ViewStatsDto;
import ru.practicum.explore.main.event.dto.response.EventFullDto;
import ru.practicum.explore.main.event.model.Event;
import ru.practicum.explore.main.participation.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventStatsService {
    private final ParticipationRequestRepository participationRequestRepository;
    private final StatsClient statsClient;

    private static final String APP_NAME = "ewm-main-service";
    private static final String EVENTS_PATH = "/events";
    private static final int DEFAULT_YEARS_RANGE = 100;

    public void enrichEventsWithStats(List<Event> events) {
        if (events.isEmpty()) {
            log.debug("Список событий для обогащения статистикой пуст");
            return;
        }

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(eventIds);
        Map<Long, Long> viewsMap = getEventsViews(events);

        events.forEach(event -> {
            event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
            event.setViews(viewsMap.getOrDefault(event.getId(), 0L));
        });

        log.debug("Обогащено статистикой {} событий", events.size());
    }

    public void enrichEventWithStats(EventFullDto eventDto, Long eventId) {
        Long confirmedRequests = getConfirmedRequestsCount(eventId);
        Long views = getEventViews(eventId);

        eventDto.setConfirmedRequests(confirmedRequests);
        eventDto.setViews(views);

        log.debug("Событие {} обогащено: подтвержденных заявок={}, просмотров={}",
                eventId, confirmedRequests, views);
    }

    public void savePublicEventsHit(String ip) {
        try {
            statsClient.saveHit(APP_NAME, EVENTS_PATH, ip);
            log.debug("Сохранен хит для публичных событий от IP: {}", ip);
        } catch (Exception e) {
            log.warn("Не удалось сохранить хит для публичных событий: {}", e.getMessage());
        }
    }

    public Long getConfirmedRequestsCount(Long eventId) {
        Long count = participationRequestRepository.countConfirmedRequestsByEventId(eventId);
        log.trace("Подтвержденных заявок для события {}: {}", eventId, count);
        return count;
    }

    public Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            log.debug("Пустой список eventIds для получения статистики заявок");
            return Map.of();
        }

        List<Object[]> results = participationRequestRepository.countConfirmedRequestsByEventIds(eventIds);
        Map<Long, Long> resultMap = results.stream()
                .collect(Collectors.toMap(
                        result -> ((Number) result[0]).longValue(),
                        result -> ((Number) result[1]).longValue()
                ));

        log.debug("Получена статистика заявок для {} событий", resultMap.size());
        return resultMap;
    }

    public Long getEventViews(Long eventId) {
        String uri = EVENTS_PATH + "/" + eventId;
        LocalDateTime start = LocalDateTime.now().minusYears(DEFAULT_YEARS_RANGE);
        LocalDateTime end = LocalDateTime.now().plusYears(DEFAULT_YEARS_RANGE);

        Map<String, Long> views = getUriHits(start, end, List.of(uri));
        Long viewCount = views.getOrDefault(uri, 0L);

        log.trace("Просмотров события {}: {}", eventId, viewCount);
        return viewCount;
    }

    public void saveEventHit(Long eventId, String ip) {
        try {
            String uri = EVENTS_PATH + "/" + eventId;
            statsClient.saveHit(APP_NAME, uri, ip);
            log.trace("Сохранен хит для события {} от IP: {}", eventId, ip);
        } catch (Exception e) {
            log.warn("Не удалось сохранить хит для события {}: {}", eventId, e.getMessage());
        }
    }

    public Map<String, Long> getUriHits(LocalDateTime start, LocalDateTime end, List<String> uris) {
        try {
            List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);
            Map<String, Long> result = stats.stream()
                    .collect(Collectors.toMap(
                            ViewStatsDto::getUri,
                            dto -> dto.getHits() != null ? dto.getHits() : 0L
                    ));

            log.debug("Получена статистика просмотров для {} URI", result.size());
            return result;
        } catch (Exception e) {
            log.warn("Ошибка при получении статистики просмотров: {}", e.getMessage());
            return Map.of();
        }
    }

    private Map<Long, Long> getEventsViews(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        List<String> uris = events.stream()
                .map(event -> EVENTS_PATH + "/" + event.getId())
                .collect(Collectors.toList());

        LocalDateTime start = LocalDateTime.now().minusYears(DEFAULT_YEARS_RANGE);
        LocalDateTime end = LocalDateTime.now().plusYears(DEFAULT_YEARS_RANGE);

        Map<String, Long> uriViews = getUriHits(start, end, uris);

        return uriViews.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getKey().startsWith(EVENTS_PATH + "/"))
                .collect(Collectors.toMap(
                        entry -> Long.parseLong(entry.getKey().replace(EVENTS_PATH + "/", "")),
                        Map.Entry::getValue
                ));
    }
}