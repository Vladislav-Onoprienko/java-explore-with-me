package ru.practicum.explore.event.service;

import ru.practicum.explore.event.dto.request.NewEventDto;
import ru.practicum.explore.event.dto.request.UpdateEventAdminRequest;
import ru.practicum.explore.event.dto.request.UpdateEventUserRequest;
import ru.practicum.explore.event.dto.response.EventFullDto;
import ru.practicum.explore.event.dto.response.EventShortDto;

import java.util.List;

public interface EventService {

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto getUserEventById(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                        String rangeStart, String rangeEnd, Integer from, Integer size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest);

    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                        String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                        String sort, Integer from, Integer size, String ip);

    EventFullDto getPublicEventById(Long eventId, String ip);
}