package ru.practicum.explore.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.dto.EndpointHitDto;
import ru.practicum.explore.dto.ViewStatsDto;
import ru.practicum.explore.server.exception.DateValidationException;
import ru.practicum.explore.server.mapper.StatsMapper;
import ru.practicum.explore.server.model.EndpointHitEntity;
import ru.practicum.explore.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto endpointHit) {
        EndpointHitEntity entity = statsMapper.toEntity(endpointHit);
        statsRepository.save(entity);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        validateDates(start, end);

        if (Boolean.TRUE.equals(unique)) {
            return (uris == null || uris.isEmpty())
                    ? statsRepository.getUniqueStatsAllUris(start, end)
                    : statsRepository.getUniqueStatsWithUris(start, end, uris);
        } else {
            return (uris == null || uris.isEmpty())
                    ? statsRepository.getStatsAllUris(start, end)
                    : statsRepository.getStatsWithUris(start, end, uris);
        }
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new DateValidationException("Даты начала и конца периода не могут быть null");
        }

        if (start.isAfter(end)) {
            throw new DateValidationException(
                    String.format("Некорректный временной период. Начало: %s, Конец: %s", start, end)
            );
        }
    }

}