package ru.practicum.explore.participation.service;

import ru.practicum.explore.participation.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore.participation.dto.EventRequestStatusUpdateResult;
import ru.practicum.explore.participation.dto.ParticipationRequestDto;

import java.util.List;

public interface EventRequestService {

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest updateRequest
    );
}
