package ru.practicum.main_server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.dto.CommentDto;
import ru.practicum.main_server.dto.EventResponseDto;
import ru.practicum.main_server.dto.EventShortDto;
import ru.practicum.main_server.service.EventService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/events")
public class EventPublicController {
    private final EventService eventService;

    public EventPublicController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping()
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam List<Long> categories,
                                         @RequestParam Boolean paid,
                                         @RequestParam(required = false) String rangeStart,
                                         @RequestParam(required = false) String rangeEnd,
                                         @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                         @RequestParam(defaultValue = "EVENT_DATE", required = false) String sort,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size,
                                         HttpServletRequest request) {
        eventService.sentHitStat(request);
        log.info("Get events(), text " + text + " , categories " + categories + " , paid " + paid +
                " , rangeStart " + rangeStart + " , rangeEnd " + rangeEnd + " , onlyAvailable " + onlyAvailable +
                " ,sort" + sort + " , from " + from + " ,size " + size);
        return eventService
                .getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/{id}")
    public EventResponseDto getEventById(@PathVariable long id, HttpServletRequest request) {
        eventService.sentHitStat(request);
        log.info("Get get event by id(), id " + id);
        return eventService.getEventById(id);
    }
}
