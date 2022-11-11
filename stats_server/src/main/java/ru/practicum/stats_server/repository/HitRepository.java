package ru.practicum.stats_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.stats_server.model.HitModel;
import ru.practicum.stats_server.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<HitModel, Long> {
    @Query("SELECT app AS app, uri AS uri, COUNT(ip) AS hits " +
            "FROM HitModel " +
            "WHERE :uris IS NULL OR uri IN :uris " +
            "AND timestamp BETWEEN :start AND :end " +
            "GROUP BY app, uri")
    List<ViewStats> findViews(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT uri AS uri, app AS app, COUNT(DISTINCT ip) AS hits " +
            "FROM HitModel " +
            "WHERE :uris IS NULL OR uri IN :uris " +
            "AND timestamp BETWEEN :start AND :end " +
            "GROUP BY app, uri, ip")
    List<ViewStats> findUniqueViews(LocalDateTime start, LocalDateTime end, List<String> uris);
}
