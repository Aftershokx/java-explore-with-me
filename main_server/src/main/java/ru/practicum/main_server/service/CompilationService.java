package ru.practicum.main_server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.dto.CompilationRequestDto;
import ru.practicum.main_server.dto.CompilationResponseDto;
import ru.practicum.main_server.dto.EventShortDto;
import ru.practicum.main_server.exception.ObjectNotFoundException;
import ru.practicum.main_server.mapper.CompilationMapper;
import ru.practicum.main_server.model.Compilation;
import ru.practicum.main_server.model.Event;
import ru.practicum.main_server.repository.CompilationRepository;
import ru.practicum.main_server.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    public List<CompilationResponseDto> getCompilations(Boolean pinned, int from, int size) {
        return compilationRepository.findAllByPinned(pinned, PageRequest.of(from / size, size))
                .stream()
                .map(CompilationMapper::toCompilationDto)
                .map(this::setViewsAndConfirmedRequestsInDto)
                .collect(Collectors.toList());
    }

    public CompilationResponseDto getCompilationById(long id) {
        return CompilationMapper.toCompilationDto(compilationRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found")));
    }

    @Transactional
    public CompilationResponseDto createCompilation(CompilationRequestDto compilationRequestDto) {
        Compilation compilation = CompilationMapper.toCompilation(compilationRequestDto);
        List<Event> events = compilationRequestDto.getEvents()
                .stream()
                .map(id -> eventRepository.findById(id)
                        .orElseThrow(() -> new ObjectNotFoundException("Compilation not found")))
                .collect(Collectors.toList());
        compilation.setEvents(events);
        Compilation newCompilation = compilationRepository.save(compilation);
        CompilationResponseDto compilationResponseDto = CompilationMapper.toCompilationDto(newCompilation);
        return setViewsAndConfirmedRequestsInDto(compilationResponseDto);
    }

    @Transactional
    public void deleteCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found"));
        compilationRepository.deleteById(compilation.getId());
    }

    @Transactional
    public void deleteEventFromCompilation(Long compId, Long eventId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        if (compilation.getEvents().contains(event)) {
            compilation.getEvents().remove(event);
        } else {
            throw new ObjectNotFoundException("Event not found");
        }
        compilationRepository.save(compilation);
    }

    @Transactional
    public void addEventToCompilation(Long compId, Long eventId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found"));
        List<Event> events = compilation.getEvents();
        events.add(eventRepository.findById(eventId).orElseThrow(() -> new ObjectNotFoundException("Event not found")));
        compilationRepository.save(compilation);
    }

    @Transactional
    public void deleteCompilationFromMainPage(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found"));
        compilation.setPinned(false);
        compilationRepository.save(compilation);
    }

    @Transactional
    public void addCompilationToMainPage(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found"));
        compilation.setPinned(true);
        compilationRepository.save(compilation);
    }

    private CompilationResponseDto setViewsAndConfirmedRequestsInDto(CompilationResponseDto compilationResponseDto) {
        List<EventShortDto> eventShortDtoList = compilationResponseDto.getEvents()
                .stream()
                .map(eventService::setConfirmedRequestsAndViewsEventShortDto)
                .collect(Collectors.toList());
        compilationResponseDto.setEvents(eventShortDtoList);
        return compilationResponseDto;
    }
}
