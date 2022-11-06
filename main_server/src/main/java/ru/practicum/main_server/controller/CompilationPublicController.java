package ru.practicum.main_server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.dto.CompilationResponseDto;
import ru.practicum.main_server.service.CompilationService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/compilations")
public class CompilationPublicController {
    private final CompilationService compilationService;

    public CompilationPublicController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @GetMapping()
    List<CompilationResponseDto> getCompilations(@RequestParam(defaultValue = "false") Boolean pinned,
                                                 @RequestParam(defaultValue = "0") int from,
                                                 @RequestParam(defaultValue = "10") int size) {
        log.info("Get compilations(), pinned " + pinned + " ,from " + from + " ,size " + size);
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{id}")
    CompilationResponseDto getCompilationById(@PathVariable long id) {
        log.info("Get compilationById() " + id);
        return compilationService.getCompilationById(id);
    }
}
