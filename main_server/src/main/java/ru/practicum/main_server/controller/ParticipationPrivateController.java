package ru.practicum.main_server.controller;

import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.dto.ParticipationRequestDto;
import ru.practicum.main_server.service.ParticipationService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/requests")
public class ParticipationPrivateController {
    private final ParticipationService participationService;

    public ParticipationPrivateController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @GetMapping
    public List<ParticipationRequestDto> getParticipationRequestsByUser(@PathVariable Long userId) {
        return participationService.getParticipationRequestsByUser(userId);
    }

    @PostMapping
    public ParticipationRequestDto createParticipationRequest(@PathVariable Long userId,
                                                              @RequestParam Long eventId) {
        return participationService.createParticipationRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        return participationService.cancelRequestByUser(userId, requestId);
    }
}
