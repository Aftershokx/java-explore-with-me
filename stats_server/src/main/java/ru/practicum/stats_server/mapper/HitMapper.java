package ru.practicum.stats_server.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.stats_server.dto.EndpointHit;
import ru.practicum.stats_server.dto.ViewStatsDto;
import ru.practicum.stats_server.model.HitModel;
import ru.practicum.stats_server.model.ViewStats;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HitMapper {

    public static HitModel toHitModel(EndpointHit endpointHit) {
        return HitModel.builder()
                .app(endpointHit.getApp())
                .ip(endpointHit.getIp())
                .uri(endpointHit.getUri())
                .timestamp(endpointHit.getTimestamp())
                .build();
    }

    public static ViewStatsDto toViewStatsDto(ViewStats viewStats) {
        return new ViewStatsDto(viewStats.getApp(), viewStats.getUri(), viewStats.getHits());
    }
}
