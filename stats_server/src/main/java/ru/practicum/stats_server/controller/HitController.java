package ru.practicum.stats_server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats_server.dto.EndpointHit;
import ru.practicum.stats_server.dto.ViewStatsDto;
import ru.practicum.stats_server.service.HitService;

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
    public void createHit(@RequestBody EndpointHit endpointHit) {
        log.info("Post create hit (), body " + endpointHit);
        hitService.createHit(endpointHit);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getViewStats(@RequestParam String start,
                                           @RequestParam String end,
                                           @RequestParam List<String> uris,
                                           @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Get get view stats(), start " + start + " , end " + end + " , uris " + uris + " ,unique " + unique);
        return hitService.getViewStats(start, end, uris, unique);
    }
}
