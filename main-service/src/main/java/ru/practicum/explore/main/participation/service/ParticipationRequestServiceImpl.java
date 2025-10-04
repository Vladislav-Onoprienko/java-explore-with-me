package ru.practicum.explore.main.participation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.main.exception.ConflictException;
import ru.practicum.explore.main.exception.EntityNotFoundException;
import ru.practicum.explore.main.exception.ForbiddenException;
import ru.practicum.explore.main.event.model.Event;
import ru.practicum.explore.main.event.model.EventState;
import ru.practicum.explore.main.event.EventRepository;
import ru.practicum.explore.main.participation.ParticipationRequestMapper;
import ru.practicum.explore.main.participation.model.ParticipationRequest;
import ru.practicum.explore.main.participation.model.RequestStatus;
import ru.practicum.explore.main.participation.dto.ParticipationRequestDto;
import ru.practicum.explore.main.participation.ParticipationRequestRepository;
import ru.practicum.explore.main.user.model.User;
import ru.practicum.explore.main.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository participationRequestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Получение заявок пользователя ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with id=" + userId + " was not found");
        }

        List<ParticipationRequest> requests = participationRequestRepository.findByRequesterId(userId);
        log.debug("Найдено заявок пользователя: {}", requests.size());

        return requests.stream()
                .map(participationRequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("Создание заявки пользователем ID: {} на событие ID: {}", userId, eventId);

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getInitiator().getId().equals(userId)) {
            log.warn("Попытка подачи заявки на свое событие. Пользователь ID: {}, событие ID: {}", userId, eventId);
            throw new ConflictException("Initiator cannot add request to own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            log.warn("Попытка подачи заявки на неопубликованное событие. Событие ID: {}", eventId);
            throw new ConflictException("Cannot participate in unpublished event");
        }

        if (participationRequestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            log.warn("Попытка подачи повторной заявки. Пользователь ID: {}, событие ID: {}", userId, eventId);
            throw new ConflictException("Request already exists");
        }

        Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(eventId);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            log.warn("Достигнут лимит участников. Событие ID: {}", eventId);
            throw new ConflictException("Participant limit reached");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(RequestStatus.PENDING)
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        ParticipationRequest savedRequest = participationRequestRepository.save(request);
        log.info("Заявка создана успешно: ID {}", savedRequest.getId());
        return participationRequestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена заявки ID: {} пользователем ID: {}", requestId, userId);

        ParticipationRequest request = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request with id=" + requestId + " was not found"));

        if (!request.getRequester().getId().equals(userId)) {
            log.warn("Попытка отмены чужой заявки. Пользователь ID: {}, заявка ID: {}", userId, requestId);
            throw new ForbiddenException("Only requester can cancel the request");
        }

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest canceledRequest = participationRequestRepository.save(request);
        log.info("Заявка отменена: ID {}", requestId);
        return participationRequestMapper.toDto(canceledRequest);
    }
}
