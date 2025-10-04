package ru.practicum.explore.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.client.StatsClient;
import ru.practicum.explore.compilation.model.Compilation;
import ru.practicum.explore.compilation.dto.NewCompilationDto;
import ru.practicum.explore.compilation.dto.UpdateCompilationRequest;
import ru.practicum.explore.compilation.dto.CompilationDto;
import ru.practicum.explore.exception.ConflictException;
import ru.practicum.explore.exception.EntityNotFoundException;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.EventRepository;
import ru.practicum.explore.participation.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final StatsClient statsClient;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Создание подборки: {}", newCompilationDto.getTitle());

        if (compilationRepository.existsByTitle(newCompilationDto.getTitle())) {
            log.warn("Попытка создания подборки с существующим заголовком: {}", newCompilationDto.getTitle());
            throw new ConflictException("Compilation with this title already exists");
        }

        Compilation compilation = compilationMapper.toEntity(newCompilationDto);

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(newCompilationDto.getEvents()));
            compilation.setEvents(events);
        } else {
            compilation.setEvents(new HashSet<>());
        }

        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Подборка создана успешно: ID {}", savedCompilation.getId());
        return compilationMapper.toDto(savedCompilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compilationId) {
        log.info("Удаление подборки: ID {}", compilationId);

        if (!compilationRepository.existsById(compilationId)) {
            log.warn("Попытка удаления несуществующей подборки: ID {}", compilationId);
            throw new EntityNotFoundException("Compilation with id=" + compilationId + " was not found");
        }

        compilationRepository.deleteById(compilationId);
        log.info("Подборка удалена: ID {}", compilationId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequest updateRequest) {
        log.info("Обновление подборки: ID {}", compilationId);

        Compilation compilation = compilationRepository.findByIdWithEvents(compilationId)
                .orElseThrow(() -> {
                    log.warn("Попытка обновления несуществующей подборки: ID {}", compilationId);
                    return new EntityNotFoundException("Compilation with id=" + compilationId + " was not found");
                });

        if (updateRequest.getTitle() != null &&
                !updateRequest.getTitle().equals(compilation.getTitle()) &&
                compilationRepository.existsByTitleAndIdNot(updateRequest.getTitle(), compilationId)) {
            log.warn("Попытка изменения заголовка на существующий: {}", updateRequest.getTitle());
            throw new ConflictException("Compilation with this title already exists");
        }

        if (updateRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(updateRequest.getEvents()));
            compilation.setEvents(events);
        }

        compilationMapper.updateCompilationFromRequest(updateRequest, compilation);
        Compilation updatedCompilation = compilationRepository.save(compilation);
        log.info("Подборка обновлена: ID {}", compilationId);

        enrichEventsWithStats(updatedCompilation.getEvents());
        return compilationMapper.toDto(updatedCompilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Получение подборок: pinned={}, from={}, size={}", pinned, from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations;

        if (pinned != null) {
            compilations = compilationRepository.findByPinnedWithEvents(pinned, pageable);
        } else {
            compilations = compilationRepository.findAllWithEvents(pageable);
        }

        List<Event> allEvents = compilations.stream()
                .flatMap(compilation -> compilation.getEvents().stream())
                .distinct()
                .collect(Collectors.toList());

        enrichEventsWithStats(allEvents);

        log.debug("Найдено подборок: {}", compilations.size());
        return compilations.stream()
                .map(compilationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compilationId) {
        log.info("Получение подборки по ID: {}", compilationId);

        Compilation compilation = compilationRepository.findByIdWithEvents(compilationId)
                .orElseThrow(() -> {
                    log.warn("Подборка не найдена: ID {}", compilationId);
                    return new EntityNotFoundException("Compilation with id=" + compilationId + " was not found");
                });

        enrichEventsWithStats(compilation.getEvents());

        return compilationMapper.toDto(compilation);
    }


    private void enrichEventsWithStats(Set<Event> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        enrichEventsWithStats(new ArrayList<>(events));
    }

    private void enrichEventsWithStats(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }

        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(eventIds);
        Map<Long, Long> viewsMap = getEventsViews(events);

        events.forEach(event -> {
            event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
            event.setViews(viewsMap.getOrDefault(event.getId(), 0L));
        });
    }

    private Map<Long, Long> getEventsViews(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        LocalDateTime start = LocalDateTime.now().minusYears(100);
        LocalDateTime end = LocalDateTime.now().plusYears(100);

        Map<String, Long> uriViews = statsClient.getUriHits(start, end, uris);

        return uriViews.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Long.parseLong(entry.getKey().replace("/events/", "")),
                        Map.Entry::getValue
                ));
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> results = participationRequestRepository.countConfirmedRequestsByEventIds(eventIds);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (Long) result[1]
                ));
    }
}