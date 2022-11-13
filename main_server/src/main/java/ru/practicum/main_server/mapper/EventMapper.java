package ru.practicum.main_server.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.main_server.dto.EventRequestDto;
import ru.practicum.main_server.dto.EventResponseDto;
import ru.practicum.main_server.dto.EventShortDto;
import ru.practicum.main_server.model.Event;
import ru.practicum.main_server.model.State;

import java.time.LocalDateTime;

@UtilityClass
public class EventMapper {

    public static EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .eventDate(event.getEventDate())
                .paid(event.isPaid())
                .title(event.getTitle())
                .confirmedRequests(event.getConfirmedReq() == null ? 0L : event.getConfirmedReq())
                .views(event.getViews() == null ? 0L : event.getViews())
                .build();
    }

    public static EventResponseDto toEventFullDto(Event event) {
        return EventResponseDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .eventDate(event.getEventDate())
                .paid(event.isPaid())
                .title(event.getTitle())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .location(event.getLocation())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn() == null ? null : event.getPublishedOn())
                .requestModeration(event.isRequestModeration())
                .state(event.getState().toString())
                .confirmedRequests(event.getConfirmedReq() == null ? 0L : event.getConfirmedReq())
                .views(event.getViews() == null ? 0L : event.getViews())
                .build();
    }

    public static Event toNewEvent(EventRequestDto eventRequestDto) {
        return Event.builder()
                .annotation(eventRequestDto.getAnnotation())
                .description(eventRequestDto.getDescription())
                .eventDate(eventRequestDto.getEventDate())
                .paid(eventRequestDto.isPaid())
                .participantLimit(eventRequestDto.getParticipantLimit())
                .requestModeration(eventRequestDto.isRequestModeration())
                .state(State.PENDING)
                .title(eventRequestDto.getTitle())
                .createdOn(LocalDateTime.now())
                .confirmedReq(eventRequestDto.getConfirmedReq() == null ? 0L : eventRequestDto.getConfirmedReq())
                .views(eventRequestDto.getViews() == null ? 0L : eventRequestDto.getViews())
                .build();
    }

}
