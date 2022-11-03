package ru.practicum.main_server.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.main_server.dto.ApiError;
import ru.practicum.main_server.exception.ObjectNotFoundException;
import ru.practicum.main_server.exception.WrongRequestException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ExceptionHandlers {
    static final String REASON_MESSAGE = "object not found";

 /*   @ExceptionHandler(ObjectNotFoundException.class)
    public ApiError notFound(RuntimeException e) {
        return ApiError.builder()
                .status(String.valueOf(HttpStatus.NOT_FOUND))
                .reason(REASON_MESSAGE)
                .message(e.getLocalizedMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }*/

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
        return new ErrorResponse(e.getMessage());
    }

    @Getter
    @Setter
    public static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
