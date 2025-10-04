package ru.practicum.explore.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explore.event.model.EventState;
import ru.practicum.explore.exception.ConflictException;
import ru.practicum.explore.exception.ValidationException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventValidator {

    public void validateEventDate(LocalDateTime eventDate, int hoursOffset, String operation) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(hoursOffset))) {
            String message = switch (operation) {
                case "user" -> "Дата события должна быть не менее чем через 2 часа от текущего момента";
                case "admin" -> "Дата события должна быть не менее чем через 1 час от публикации";
                default -> "Некорректная дата события";
            };
            throw new ValidationException(message);
        }
    }

    public void validateParticipantLimit(Integer participantLimit) {
        if (participantLimit != null && participantLimit < 0) {
            throw new ValidationException("Лимит участников не может быть отрицательным");
        }
    }

    public void validateEventStateForUserUpdate(EventState currentState) {
        if (currentState == EventState.PUBLISHED) {
            throw new ConflictException("Можно изменять только ожидающие или отмененные события");
        }
    }

    public void validateEventStateForAdminPublish(EventState currentState) {
        if (currentState != EventState.PENDING) {
            throw new ConflictException("Можно публиковать только события в состоянии ожидания");
        }
    }

    public void validateEventStateForAdminReject(EventState currentState) {
        if (currentState == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя отклонить уже опубликованное событие");
        }
    }
}
