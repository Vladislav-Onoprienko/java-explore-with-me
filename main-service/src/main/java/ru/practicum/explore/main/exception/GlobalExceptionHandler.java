package ru.practicum.explore.main.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(EntityNotFoundException e) {
        log.error("Not Found: {}", e.getMessage());
        return new ApiError(
                e.getMessage(),
                "The required object was not found.",
                "NOT_FOUND"
        );
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(Exception e) {
        log.error("Conflict: {}", e.getMessage());
        return new ApiError(
                e.getMessage(),
                "Integrity constraint has been violated.",
                "CONFLICT"
        );
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(ValidationException e) {
        log.error("Bad Request: {}", e.getMessage());
        return new ApiError(
                e.getMessage(),
                "Incorrectly made request.",
                "BAD_REQUEST"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.error("Validation error: {}", e.getMessage());

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("Field: %s. Error: %s. Value: %s",
                        error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                .findFirst()
                .orElse("Validation failed");

        return new ApiError(
                errorMessage,
                "Incorrectly made request.",
                "BAD_REQUEST"
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbidden(ForbiddenException e) {
        log.error("Forbidden: {}", e.getMessage());
        return new ApiError(
                e.getMessage(),
                "For the requested operation the conditions are not met.",
                "FORBIDDEN"
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleInternalError(Exception e) {
        log.error("Internal Error: {}", e.getMessage(), e);  // ← полный stacktrace
        return new ApiError(
                "Internal server error: " + e.getClass().getSimpleName() + ": " + e.getMessage(),
                "Internal server error",
                "INTERNAL_SERVER_ERROR"
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingParams(MissingServletRequestParameterException e) {
        log.error("Missing parameter: {}", e.getMessage());
        return new ApiError(
                "Missing required parameter: " + e.getParameterName(),
                "Incorrectly made request.",
                "BAD_REQUEST"
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error("Type mismatch: {}", e.getMessage());
        return new ApiError(
                "Invalid parameter value: " + e.getName(),
                "Incorrectly made request.",
                "BAD_REQUEST"
        );
    }
}