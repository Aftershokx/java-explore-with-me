package ru.practicum.main_server.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.main_server.dto.CompilationResponseDto;
import ru.practicum.main_server.dto.EventShortDto;
import ru.practicum.main_server.dto.CompilationRequestDto;
import ru.practicum.main_server.model.Compilation;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {
    public static Compilation toCompilation(CompilationRequestDto compilationRequestDto) {
        return Compilation.builder()
                .title(compilationRequestDto.getTitle())
                .pinned(compilationRequestDto.isPinned())
                .build();
    }

    public static CompilationResponseDto toCompilationDto(Compilation compilation) {
        List<EventShortDto> eventShortDtos = compilation.getEvents()
                .stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
        return CompilationResponseDto.builder()
                .id(compilation.getId())
                .events(eventShortDtos)
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .build();
    }
}
