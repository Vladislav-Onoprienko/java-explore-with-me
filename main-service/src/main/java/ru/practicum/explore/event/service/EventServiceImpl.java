package ru.practicum.explore.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.category.model.Category;
import ru.practicum.explore.category.CategoryRepository;
import ru.practicum.explore.event.EventMapper;
import ru.practicum.explore.event.EventRepository;
import ru.practicum.explore.event.EventValidator;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.model.EventState;
import ru.practicum.explore.event.dto.request.NewEventDto;
import ru.practicum.explore.event.dto.request.UpdateEventAdminRequest;
import ru.practicum.explore.event.dto.request.UpdateEventUserRequest;
import ru.practicum.explore.event.dto.response.EventFullDto;
import ru.practicum.explore.event.dto.response.EventShortDto;
import ru.practicum.explore.exception.ConflictException;
import ru.practicum.explore.exception.EntityNotFoundException;
import ru.practicum.explore.exception.ForbiddenException;
import ru.practicum.explore.exception.ValidationException;
import ru.practicum.explore.user.model.User;
import ru.practicum.explore.user.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final EventStatsService eventStatsService;
    private final EventValidator eventValidator;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        log.info("Создание события пользователем ID: {}", userId);

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", userId);
                    return new EntityNotFoundException("Пользователь с id=" + userId + " не найден");
                });

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> {
                    log.warn("Категория с id={} не найдена", newEventDto.getCategory());
                    return new EntityNotFoundException("Категория с id=" + newEventDto.getCategory() + " не найдена");
                });

        eventValidator.validateEventDate(newEventDto.getEventDate(), 2, "user");
        eventValidator.validateParticipantLimit(newEventDto.getParticipantLimit());

        if (newEventDto.getLocation() == null) {
            throw new ConflictException("Местоположение обязательно для указания");
        }

        Event event = eventMapper.toEntity(newEventDto);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        Event savedEvent = eventRepository.save(event);
        log.info("Событие создано: ID {}, название: {}", savedEvent.getId(), savedEvent.getTitle());

        return eventMapper.toFullDto(savedEvent);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.info("Получение событий пользователя ID: {}", userId);

        checkUserExists(userId);

        Pageable pageable = createPageable(from, size);
        List<Event> events = eventRepository.findByInitiatorIdWithCategoryAndInitiator(userId, pageable);

        log.debug("Найдено событий пользователя {}: {}", userId, events.size());

        eventStatsService.enrichEventsWithStats(events);
        return events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        log.info("Получение события ID: {} пользователя ID: {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorIdWithCategoryAndInitiator(eventId, userId)
                .orElseThrow(() -> {
                    log.warn("Событие с id={} не найдено для пользователя ID: {}", eventId, userId);
                    return new EntityNotFoundException("Событие с id=" + eventId + " не найдено");
                });

        EventFullDto result = eventMapper.toFullDto(event);
        eventStatsService.enrichEventWithStats(result, event.getId());

        return result;
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                               String rangeStart, String rangeEnd, Integer from, Integer size) {
        log.info("Поиск событий администратором: users={}, categories={}", users, categories);

        LocalDateTime startTime = parseDateTime(rangeStart);
        LocalDateTime endTime = parseDateTime(rangeEnd);

        validateDateRange(startTime, endTime);

        Pageable pageable = createPageable(from, size);
        final List<EventState> stateEnums = parseEventStates(states);

        List<Event> allEvents = eventRepository.findAllEventsWithCategoryAndInitiator(pageable);

        List<Event> events = applyAdminFilters(allEvents, users, stateEnums, categories, startTime, endTime);

        log.debug("Найдено событий после фильтрации: {}", events.size());

        eventStatsService.enrichEventsWithStats(events);
        return events.stream()
                .map(eventMapper::toFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                               String sort, Integer from, Integer size, String ip) {
        log.info("Публичный поиск событий: text={}, categories={}, IP: {}", text, categories, ip);

        LocalDateTime startTime = parseDateTime(rangeStart);
        LocalDateTime endTime = parseDateTime(rangeEnd);

        validateDateRange(startTime, endTime);

        Pageable pageable = createPageable(from, size);

        List<Event> events = eventRepository.findPublishedEventsWithCategoryAndInitiator(pageable);
        log.debug("Найдено опубликованных событий: {}", events.size());

        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        final Map<Long, Long> confirmedRequestsMap = eventStatsService.getConfirmedRequestsMap(eventIds);

        events = applyPublicFilters(events, text, categories, paid, startTime, endTime, onlyAvailable,
                confirmedRequestsMap);
        log.debug("Событий после фильтрации: {}", events.size());

        saveEventsHits(events, ip);
        eventStatsService.enrichEventsWithStats(events);

        List<EventShortDto> result = events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());

        sortEvents(result, sort);
        eventStatsService.savePublicEventsHit(ip);

        return result;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        log.info("Обновление события ID: {} пользователем ID: {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorIdWithCategoryAndInitiator(eventId, userId)
                .orElseThrow(() -> {
                    log.warn("Событие с id={} не найдено для пользователя ID: {}", eventId, userId);
                    return new EntityNotFoundException("Событие с id=" + eventId + " не найдено");
                });

        validateUserIsInitiator(event, userId);
        eventValidator.validateEventStateForUserUpdate(event.getState());
        eventValidator.validateEventDate(updateRequest.getEventDate(), 2, "user");
        eventValidator.validateParticipantLimit(updateRequest.getParticipantLimit());

        updateEventCategory(event, updateRequest.getCategory());
        updateEventStateByUser(event, updateRequest.getStateAction());

        eventMapper.updateEventFromUserRequest(updateRequest, event);
        Event updatedEvent = eventRepository.save(event);

        log.info("Событие {} обновлено пользователем, состояние: {}", eventId, updatedEvent.getState());

        EventFullDto result = eventMapper.toFullDto(updatedEvent);
        eventStatsService.enrichEventWithStats(result, eventId);
        return result;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        log.info("Обновление события ID: {} администратором", eventId);

        Event event = eventRepository.findByIdWithCategoryAndInitiator(eventId)
                .orElseThrow(() -> {
                    log.warn("Событие с id={} не найдено", eventId);
                    return new EntityNotFoundException("Событие с id=" + eventId + " не найдено");
                });

        eventValidator.validateParticipantLimit(updateRequest.getParticipantLimit());
        eventValidator.validateEventDate(updateRequest.getEventDate(), 1, "admin");

        updateEventStateByAdmin(event, updateRequest.getStateAction());
        updateEventCategory(event, updateRequest.getCategory());

        eventMapper.updateEventFromAdminRequest(updateRequest, event);
        Event updatedEvent = eventRepository.save(event);

        log.info("Событие {} обновлено администратором, состояние: {}", eventId, updatedEvent.getState());

        EventFullDto result = eventMapper.toFullDto(updatedEvent);
        eventStatsService.enrichEventWithStats(result, eventId);
        return result;
    }

    @Override
    public EventFullDto getPublicEventById(Long eventId, String ip) {
        log.info("Получение публичного события ID: {}, IP: {}", eventId, ip);

        Event event = eventRepository.findByIdWithCategoryAndInitiator(eventId)
                .orElseThrow(() -> {
                    log.warn("Событие с id={} не найдено", eventId);
                    return new EntityNotFoundException("Событие с id=" + eventId + " не найдено");
                });

        if (event.getState() != EventState.PUBLISHED) {
            throw new EntityNotFoundException("Событие с id=" + eventId + " не найдено");
        }

        eventStatsService.saveEventHit(eventId, ip);

        EventFullDto result = eventMapper.toFullDto(event);
        eventStatsService.enrichEventWithStats(result, eventId);

        log.debug("Событие {}: просмотры={}, заявки={}", eventId, result.getViews(), result.getConfirmedRequests());
        return result;
    }


    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("Пользователь с id={} не найден", userId);
            throw new EntityNotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    private void validateUserIsInitiator(Event event, Long userId) {
        if (!event.getInitiator().getId().equals(userId)) {
            log.warn("Попытка изменения чужого события. Пользователь ID: {}, событие ID: {}", userId, event.getId());
            throw new ForbiddenException("Только инициатор события может его изменять");
        }
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            log.warn("Некорректный диапазон дат: start={}, end={}", start, end);
            throw new ValidationException("Начало диапазона не может быть позже конца диапазона");
        }
    }

    private List<EventState> parseEventStates(List<String> states) {
        return (states != null && !states.isEmpty())
                ? states.stream().map(EventState::valueOf).toList()
                : null;
    }

    private List<Event> applyAdminFilters(List<Event> events, List<Long> users, List<EventState> states,
                                          List<Long> categories, LocalDateTime start, LocalDateTime end) {
        return events.stream()
                .filter(event -> users == null || users.isEmpty() || users.contains(event.getInitiator().getId()))
                .filter(event -> states == null || states.isEmpty() || states.contains(event.getState()))
                .filter(event -> categories == null || categories.isEmpty() || categories.contains(event.getCategory().getId()))
                .filter(event -> start == null || !event.getEventDate().isBefore(start))
                .filter(event -> end == null || !event.getEventDate().isAfter(end))
                .collect(Collectors.toList());
    }

    private List<Event> applyPublicFilters(List<Event> events, String text, List<Long> categories, Boolean paid,
                                           LocalDateTime start, LocalDateTime end, Boolean onlyAvailable,
                                           Map<Long, Long> confirmedRequests) {
        final String searchText = (text != null && !text.isEmpty()) ? text.toLowerCase() : null;

        return events.stream()
                .filter(event -> searchText == null ||
                        event.getAnnotation().toLowerCase().contains(searchText) ||
                        (event.getDescription() != null && event.getDescription().toLowerCase().contains(searchText)))
                .filter(event -> categories == null || categories.isEmpty() || categories.contains(event.getCategory().getId()))
                .filter(event -> paid == null || event.getPaid().equals(paid))
                .filter(event -> start == null || !event.getEventDate().isBefore(start))
                .filter(event -> end == null || !event.getEventDate().isAfter(end))
                .filter(event -> !Boolean.TRUE.equals(onlyAvailable) ||
                        event.getParticipantLimit() == 0 ||
                        event.getParticipantLimit() > confirmedRequests.getOrDefault(event.getId(), 0L))
                .collect(Collectors.toList());
    }

    private void saveEventsHits(List<Event> events, String ip) {
        for (Event event : events) {
            try {
                eventStatsService.saveEventHit(event.getId(), ip);
            } catch (Exception e) {
                log.warn("Ошибка сохранения статистики для события {}: {}", event.getId(), e.getMessage());
            }
        }
    }

    private void updateEventCategory(Event event, Long categoryId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> {
                        log.warn("Категория с id={} не найдена", categoryId);
                        return new EntityNotFoundException("Категория не найдена");
                    });
            event.setCategory(category);
        }
    }

    private void updateEventStateByUser(Event event, String stateAction) {
        if (stateAction != null) {
            switch (stateAction) {
                case "SEND_TO_REVIEW":
                    event.setState(EventState.PENDING);
                    break;
                case "CANCEL_REVIEW":
                    event.setState(EventState.CANCELED);
                    break;
            }
        }
    }

    private void updateEventStateByAdmin(Event event, String stateAction) {
        if (stateAction != null) {
            switch (stateAction) {
                case "PUBLISH_EVENT":
                    eventValidator.validateEventStateForAdminPublish(event.getState());
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case "REJECT_EVENT":
                    eventValidator.validateEventStateForAdminReject(event.getState());
                    event.setState(EventState.CANCELED);
                    break;
            }
        }
    }

    private Pageable createPageable(Integer from, Integer size) {
        if (from == null) from = 0;
        if (size == null) size = 10;
        if (size == 0) size = 10;
        if (from < 0) from = 0;
        if (size <= 0) size = 10;

        int page = from / size;
        return PageRequest.of(page, size);
    }

    private LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null) return null;
        try {
            return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Не удалось распарсить дату: {}, используется null", dateTime);
            return null;
        }
    }

    private void sortEvents(List<EventShortDto> events, String sort) {
        if ("VIEWS".equals(sort)) {
            events.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        } else if ("EVENT_DATE".equals(sort)) {
            events.sort(Comparator.comparing(EventShortDto::getEventDate));
        }
    }
}