package ru.practicum.main_server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.dto.CompilationRequestDto;
import ru.practicum.main_server.dto.CompilationResponseDto;
import ru.practicum.main_server.service.CompilationService;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "admin/compilations")
public class CompilationAdminController {
    private final CompilationService compilationService;

    public CompilationAdminController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping
    public CompilationResponseDto createCompilation(@RequestBody @Valid CompilationRequestDto compilationRequestDto) {
        log.info("Post compilation() " + compilationRequestDto);
        return compilationService.createCompilation(compilationRequestDto);
    }

    @DeleteMapping("/{compId}")
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("Delete compilationById() " + compId);
        compilationService.deleteCompilation(compId);
    }

    @DeleteMapping("/{compId}/events/{eventId}")
    public void deleteEventFromCompilation(@PathVariable Long compId,
                                           @PathVariable Long eventId) {
        log.info("Delete event from compilation(), compilationId " + compId + " , eventId " + eventId);
        compilationService.deleteEventFromCompilation(compId, eventId);
    }

    @PatchMapping("/{compId}/events/{eventId}")
    public void addEventToCompilation(@PathVariable Long compId,
                                      @PathVariable Long eventId) {
        log.info("Patch add event to compilation (), compilationId " + compId + " , eventId " + eventId);
        compilationService.addEventToCompilation(compId, eventId);
    }

    @DeleteMapping("/{compId}/pin")
    public void deleteCompilationFromMainPage(@PathVariable Long compId) {
        log.info("Delete compilation from main page() " + compId);
        compilationService.deleteCompilationFromMainPage(compId);
    }

    @PatchMapping("/{compId}/pin")
    public void addCompilationToMainPage(@PathVariable Long compId) {
        log.info("Patch add compilation to main page() " + compId);
        compilationService.addCompilationToMainPage(compId);
    }
}
