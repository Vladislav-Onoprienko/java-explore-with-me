package ru.practicum.explore.participation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.participation.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore.participation.dto.EventRequestStatusUpdateResult;
import ru.practicum.explore.participation.dto.ParticipationRequestDto;
import ru.practicum.explore.participation.service.EventRequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events/{eventId}/requests")
public class EventRequestController {

    private final EventRequestService eventRequestService;

    @GetMapping
    public List<ParticipationRequestDto> getEventRequests(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}/requests - получение заявок на участие", userId, eventId);
        return eventRequestService.getEventRequests(userId, eventId);
    }

    @PatchMapping
    public EventRequestStatusUpdateResult updateRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        log.info("PATCH /users/{}/events/{}/requests - обновление статуса заявок", userId, eventId);
        return eventRequestService.updateRequestStatus(userId, eventId, updateRequest);
    }
}