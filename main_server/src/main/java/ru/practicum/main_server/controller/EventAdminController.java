package ru.practicum.main_server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.dto.AdminUpdateEventRequest;
import ru.practicum.main_server.dto.CommentDto;
import ru.practicum.main_server.dto.EventResponseDto;
import ru.practicum.main_server.mapper.CommentMapper;
import ru.practicum.main_server.model.State;
import ru.practicum.main_server.service.EventService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/admin/events")
public class EventAdminController {
    private final EventService eventService;

    public EventAdminController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventResponseDto> getEvents(@RequestParam List<Long> users,
                                            @RequestParam(required = false) List<State> states,
                                            @RequestParam List<Long> categories,
                                            @RequestParam(required = false) String rangeStart,
                                            @RequestParam(required = false) String rangeEnd,
                                            @RequestParam(defaultValue = "0", required = false) int from,
                                            @RequestParam(defaultValue = "10", required = false) int size) {
        log.info("Get events(), users " + users + " , states " + states + " , categories " + categories +
                " , rangeStart " + rangeStart + " , rangeEnd " + rangeEnd + " , from " + from + " ,size " + size);
        return eventService.getAdminEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PutMapping("/{eventId}")
    public EventResponseDto updateEventByAdmin(@PathVariable Long eventId,
                                               @RequestBody AdminUpdateEventRequest adminUpdateEventRequest) {
        log.info("Put update event by admin(), eventId " + eventId + " , body " + adminUpdateEventRequest);
        return eventService.updateEventByAdmin(eventId, adminUpdateEventRequest);
    }

    @PatchMapping("/{eventId}/publish")
    public EventResponseDto publishEventByAdmin(@PathVariable Long eventId) {
        log.info("Patch publish event() " + eventId);
        return eventService.publishEventByAdmin(eventId);
    }

    @PatchMapping("/{eventId}/reject")
    public EventResponseDto rejectEventByAdmin(@PathVariable Long eventId) {
        log.info("Patch reject event by admin() " + eventId);
        return eventService.rejectEventByAdmin(eventId);
    }

    @PatchMapping("/{eventId}/comments/{commentId}")
    public CommentDto updateComment(@PathVariable long eventId,
                                    @PathVariable long commentId,
                                    @RequestBody CommentDto commentDto) {
        log.info("Patch update comment(), eventId " + eventId + " , commentId " + commentId +
                " , body " + commentDto);
        return CommentMapper.toCommentDto(eventService.updateCommentByAdmin(eventId, commentId, commentDto));
    }

    @DeleteMapping("/{eventId}/comments/{commentId}")
    public void deleteComment(@PathVariable long eventId,
                              @PathVariable long commentId) {
        log.info("Delete delete comment(), eventId " + eventId + " , commentId " + commentId);
        eventService.deleteComment(eventId, commentId);
    }

}
