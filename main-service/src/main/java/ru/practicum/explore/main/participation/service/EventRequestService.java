package ru.practicum.explore.main.participation.service;

import ru.practicum.explore.main.participation.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore.main.participation.dto.EventRequestStatusUpdateResult;
import ru.practicum.explore.main.participation.dto.ParticipationRequestDto;

import java.util.List;

public interface EventRequestService {

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest updateRequest
    );
}
