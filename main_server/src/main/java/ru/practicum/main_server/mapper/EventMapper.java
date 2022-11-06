package ru.practicum.main_server.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.main_server.dto.EventResponseDto;
import ru.practicum.main_server.dto.EventShortDto;
import ru.practicum.main_server.dto.EventRequestDto;
import ru.practicum.main_server.model.Event;
import ru.practicum.main_server.model.State;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class EventMapper {

    public static EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .eventDate(event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .paid(event.isPaid())
                .title(event.getTitle())
                .build();
    }

    public static EventResponseDto toEventFullDto(Event event) {
        return EventResponseDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .eventDate(event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .paid(event.isPaid())
                .title(event.getTitle())
                .createdOn(event.getCreatedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .description(event.getDescription())
                .location(event.getLocation())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn() == null ? null : event.getPublishedOn()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .requestModeration(event.isRequestModeration())
                .state(event.getState().toString())
                .build();
    }

    public static Event toNewEvent(EventRequestDto eventRequestDto) {
        return Event.builder()
                .annotation(eventRequestDto.getAnnotation())
                .description(eventRequestDto.getDescription())
                .eventDate(LocalDateTime.parse(eventRequestDto.getEventDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .paid(eventRequestDto.isPaid())
                .participantLimit(eventRequestDto.getParticipantLimit())
                .requestModeration(eventRequestDto.isRequestModeration())
                .state(State.PENDING)
                .title(eventRequestDto.getTitle())
                .createdOn(LocalDateTime.now())
                .build();
    }
}
