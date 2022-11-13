package ru.practicum.main_server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.dto.ParticipationRequestDto;
import ru.practicum.main_server.service.ParticipationService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users/{userId}/requests")
public class ParticipationPrivateController {
    private final ParticipationService participationService;

    public ParticipationPrivateController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @GetMapping
    public List<ParticipationRequestDto> getParticipationRequestsByUser(@PathVariable Long userId) {
        log.info("Get get participation requests by user(), userId " + userId);
        return participationService.getParticipationRequestsByUser(userId);
    }

    @PostMapping
    public ParticipationRequestDto createParticipationRequest(@PathVariable Long userId,
                                                              @RequestParam Long eventId) {
        log.info("Post create participation request(), userId " + userId + " , eventId " + eventId);
        return participationService.createParticipationRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        log.info("Patch cancel request(), userId " + userId + " , requestId " + requestId);
        return participationService.cancelRequestByUser(userId, requestId);
    }
}
