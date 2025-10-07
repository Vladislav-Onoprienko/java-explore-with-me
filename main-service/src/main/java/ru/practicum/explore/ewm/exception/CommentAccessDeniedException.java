package ru.practicum.explore.ewm.exception;

public class CommentAccessDeniedException extends ForbiddenException {
    public CommentAccessDeniedException(String message) {
        super(message);
    }
}
