package ru.practicum.stats_server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats_server.dto.EndpointHit;
import ru.practicum.stats_server.dto.ViewStats;
import ru.practicum.stats_server.service.HitService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
public class HitController {
    private final HitService hitService;

    @Autowired
    public HitController(HitService hitService) {
        this.hitService = hitService;
    }

    @PostMapping("/hit")
    public EndpointHit createHit(@RequestBody @Valid EndpointHit endpointHit) {
        log.info("Post create hit (), body " + endpointHit);
        return hitService.createHit(endpointHit);
    }

    @GetMapping("/stats")
    public List<ViewStats> getViewStats(@RequestParam String start,
                                        @RequestParam String end,
                                        @RequestParam List<String> uris,
                                        @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Get get view stats(), start " + start + " , end " + end + " , uris " + uris + " ,unique " + unique);
        return hitService.getViewStats(start, end, uris, unique);
    }
}
