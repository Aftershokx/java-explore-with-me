package ru.practicum.main_server.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.main_server.dto.ApiError;
import ru.practicum.main_server.exception.ObjectNotFoundException;
import ru.practicum.main_server.exception.WrongRequestException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestControllerAdvice
public class ExceptionHandlers {
    static final String REASON_MESSAGE = "object not found";

    @ExceptionHandler(WrongRequestException.class)
    public ApiError forbidden(RuntimeException e) {
        return ApiError.builder()
                .status(String.valueOf(HttpStatus.FORBIDDEN))
                .reason(REASON_MESSAGE)
                .message(e.getLocalizedMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleObjectNotFoundException(ObjectNotFoundException e) {
        return new ErrorResponse(e.getMessage(), "Object not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        return new ErrorResponse(Objects.requireNonNull(e.getFieldError()).getDefaultMessage(),
                "Field error in object", HttpStatus.BAD_REQUEST);
    }

    @Getter
    @Setter
    public static class ErrorResponse {
        private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        private List<String> errors = new ArrayList<>();
        private String message;
        private String reason;
        private HttpStatus status;
        private String timestamp;

        public ErrorResponse(String message, String reason, HttpStatus status) {
            this.message = message;
            this.reason = reason;
            this.status = status;
            this.timestamp = LocalDateTime.now().format(DATE_TIME);
        }

    }
}
