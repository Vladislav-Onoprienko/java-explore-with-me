package ru.practicum.explore.main.participation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.main.exception.ConflictException;
import ru.practicum.explore.main.exception.EntityNotFoundException;
import ru.practicum.explore.main.exception.ForbiddenException;
import ru.practicum.explore.main.event.model.Event;
import ru.practicum.explore.main.event.EventRepository;
import ru.practicum.explore.main.participation.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore.main.participation.dto.EventRequestStatusUpdateResult;
import ru.practicum.explore.main.participation.dto.ParticipationRequestDto;
import ru.practicum.explore.main.participation.ParticipationRequestMapper;
import ru.practicum.explore.main.participation.model.ParticipationRequest;
import ru.practicum.explore.main.participation.model.RequestStatus;
import ru.practicum.explore.main.participation.ParticipationRequestRepository;
import ru.practicum.explore.main.user.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventRequestServiceImpl implements EventRequestService {

    private final ParticipationRequestRepository participationRequestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Получение заявок на участие в событии ID: {} пользователем ID: {}", eventId, userId);

        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with id=" + userId + " was not found");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Only event initiator can view event requests");
        }

        List<ParticipationRequest> requests = participationRequestRepository.findByEventId(eventId);
        log.debug("Найдено заявок на событие: {}", requests.size());

        return requests.stream()
                .map(participationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        log.info("Обновление статуса заявок на событие ID: {} пользователем ID: {}", eventId, userId);

        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with id=" + userId + " was not found");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Only event initiator can update request status");
        }

        List<ParticipationRequest> requests = participationRequestRepository.findAllById(updateRequest.getRequestIds());

        requests.forEach(request -> {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Request with id=" + request.getId() + " does not belong to event with id=" + eventId);
            }
        });

        requests.forEach(request -> {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
        });

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        if (updateRequest.getStatus() == RequestStatus.CONFIRMED) {
            Long confirmedCount = participationRequestRepository.countConfirmedRequestsByEventId(eventId);

            for (ParticipationRequest request : requests) {
                if (event.getParticipantLimit() == 0 || confirmedCount < event.getParticipantLimit()) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests.add(participationRequestMapper.toDto(request));
                    confirmedCount++;
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(participationRequestMapper.toDto(request));
                }
            }

            if (confirmedCount >= event.getParticipantLimit() && event.getParticipantLimit() > 0) {
                List<ParticipationRequest> pendingRequests = participationRequestRepository
                        .findByEventIdAndStatus(eventId, RequestStatus.PENDING);
                pendingRequests.forEach(req -> {
                    if (req.getStatus() == RequestStatus.PENDING) {
                        req.setStatus(RequestStatus.REJECTED);
                        rejectedRequests.add(participationRequestMapper.toDto(req));
                    }
                });
            }
        } else if (updateRequest.getStatus() == RequestStatus.REJECTED) {
            for (ParticipationRequest request : requests) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(participationRequestMapper.toDto(request));
            }
        }

        participationRequestRepository.saveAll(requests);
        log.info("Статусы заявок обновлены. Подтверждено: {}, Отклонено: {}",
                confirmedRequests.size(), rejectedRequests.size());

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }
}
