package ru.practicum.stats_server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats_server.dto.EndpointHit;
import ru.practicum.stats_server.dto.ViewStatsDto;
import ru.practicum.stats_server.mapper.HitMapper;
import ru.practicum.stats_server.model.HitModel;
import ru.practicum.stats_server.repository.HitRepository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HitService {
    private final HitRepository hitRepository;

    @Transactional
    public void createHit(EndpointHit endpointHit) {
        HitModel hitModel = HitMapper.toHitModel(endpointHit);
        hitRepository.save(hitModel);
    }

    public List<ViewStatsDto> getViewStats(String start, String end, List<String> uris, boolean unique) {
        LocalDateTime startDate = toLocalDateTime(start);
        LocalDateTime endDate = toLocalDateTime(end);
        if (unique) {
            return hitRepository.findUniqueViews(startDate, endDate, uris)
                    .stream()
                    .map(HitMapper::toViewStatsDto)
                    .collect(Collectors.toList());
        } else {
            return hitRepository.findViews(startDate, endDate, uris)
                    .stream()
                    .map(HitMapper::toViewStatsDto)
                    .collect(Collectors.toList());
        }
    }

    private LocalDateTime toLocalDateTime(String date) {
        String decodeDate = URLDecoder.decode(date, StandardCharsets.UTF_8);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(decodeDate, formatter);
    }
}
