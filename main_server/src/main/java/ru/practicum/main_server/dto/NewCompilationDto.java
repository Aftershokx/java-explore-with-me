package ru.practicum.main_server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {
    private List<Long> events;
    private String title;
    private boolean pinned;
}
