package ru.practicum.main_server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.dto.*;
import ru.practicum.main_server.service.EventService;
import ru.practicum.main_server.service.ParticipationService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users/{userId}/events")
public class EventPrivateController {
    private final EventService eventService;
    private final ParticipationService participationService;

    public EventPrivateController(EventService eventService, ParticipationService participationService) {
        this.eventService = eventService;
        this.participationService = participationService;
    }

    @GetMapping()
    List<EventShortDto> getCurrentUserEvents(@PathVariable long userId,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        log.info("Get current user events(), userId " + userId + " ,from " + from + " ,size " + size);
        return eventService
                .getEventsCurrentUser(userId, from, size);
    }

    @PatchMapping
    public EventResponseDto updateEvent(@PathVariable Long userId,
                                        @RequestBody UpdateEventRequest updateEventRequest) {
        log.info("Patch update event(), userId " + userId + " , body " + updateEventRequest);
        return eventService.updateEvent(userId, updateEventRequest);
    }

    @PostMapping
    public EventResponseDto createEvent(@PathVariable Long userId,
                                        @RequestBody @Valid EventRequestDto eventRequestDto) {
        log.info("Post create event(), userId " + userId + " , body " + eventRequestDto);
        return eventService.createEvent(userId, eventRequestDto);
    }

    @GetMapping("/{eventId}")
    public EventResponseDto getEventCurrentUser(@PathVariable Long userId,
                                                @PathVariable Long eventId) {
        log.info("Get event(), userId " + userId + " , eventId " + eventId);
        return eventService.getEventCurrentUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventResponseDto cancelEvent(@PathVariable Long userId,
                                        @PathVariable Long eventId) {
        log.info("Patch cancel event(), userId " + userId + " , eventId " + eventId);
        return eventService.cancelEvent(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipationByOwner(@PathVariable Long userId,
                                                                      @PathVariable Long eventId) {
        log.info("Get event participation by owner(), userId " + userId + " , eventId " + eventId);
        return participationService.getEventParticipationByOwner(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests/{reqId}/confirm")
    public ParticipationRequestDto approvalParticipationEventRequest(@PathVariable Long userId,
                                                                     @PathVariable Long eventId,
                                                                     @PathVariable Long reqId) {
        log.info("Patch approval participation event request(), userId " + userId + " , eventId " + eventId +
                " , reqId " + reqId);
        return participationService.approvalParticipationEventRequest(userId, eventId, reqId);
    }

    @PatchMapping("/{eventId}/requests/{reqId}/reject")
    public ParticipationRequestDto rejectParticipationEventRequest(@PathVariable Long userId,
                                                                   @PathVariable Long eventId,
                                                                   @PathVariable Long reqId) {
        log.info("Patch reject participation event request(), userId " + userId + " , eventId " + eventId +
                " , reqId " + reqId);
        return participationService.rejectParticipationEventRequest(userId, eventId, reqId);
    }
}
