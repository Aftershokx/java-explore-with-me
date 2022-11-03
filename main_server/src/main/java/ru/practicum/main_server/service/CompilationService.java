package ru.practicum.main_server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.dto.CompilationDto;
import ru.practicum.main_server.dto.EventShortDto;
import ru.practicum.main_server.dto.NewCompilationDto;
import ru.practicum.main_server.exception.ObjectNotFoundException;
import ru.practicum.main_server.mapper.CompilationMapper;
import ru.practicum.main_server.model.Compilation;
import ru.practicum.main_server.model.Event;
import ru.practicum.main_server.repository.CompilationRepository;
import ru.practicum.main_server.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        log.info("getCompilations");
        return compilationRepository.findAllByPinned(pinned, PageRequest.of(from / size, size))
                .stream()
                .map(CompilationMapper::toCompilationDto)
                .map(this::setViewsAndConfirmedRequestsInDto)
                .collect(Collectors.toList());
    }

    public CompilationDto getCompilationById(long id) {
        log.info("getCompilationById");
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilationRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found")));
        log.info("getcompilation " + compilationRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found")));
        CompilationDto updated = setViewsAndConfirmedRequestsInDto(compilationDto);
        if (compilationDto.getEvents().size() > 0) {
            log.info("updated" + updated);
            return updated;
        }
        return compilationDto;
    }

    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("create compilation");
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        List<Event> events = newCompilationDto.getEvents()
                .stream()
                .map(id -> eventRepository.findById(id)
                        .orElseThrow(() -> new ObjectNotFoundException("Compilation not found")))
                .collect(Collectors.toList());
        compilation.setEvents(events);
        Compilation newCompilation = compilationRepository.save(compilation);
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(newCompilation);
        return setViewsAndConfirmedRequestsInDto(compilationDto);
    }

    @Transactional
    public void deleteCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found"));
        compilationRepository.delete(compilation);
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
        log.info("save" + compilation);
        log.info("save check "+compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found")));
    }

    public void addEventToCompilation(Long compId, Long eventId) {
        log.info("add event to compilation");
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found"));
        List<Event> events = compilation.getEvents();
        events.add(eventRepository.findById(eventId).orElseThrow(() -> new ObjectNotFoundException("Event not found")));
        compilationRepository.save(compilation);
    }

    public void deleteCompilationFromMainPage(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found"));
        compilation.setPinned(false);
        compilationRepository.save(compilation);
    }

    public void addCompilationToMainPage(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found"));
        compilation.setPinned(true);
        compilationRepository.save(compilation);
    }

    private CompilationDto setViewsAndConfirmedRequestsInDto(CompilationDto compilationDto) {
        log.info("setvievs");
        List<EventShortDto> eventShortDtos = compilationDto.getEvents()
                .stream()
                .map(eventService::setConfirmedRequestsAndViewsEventShortDto)
                .collect(Collectors.toList());
        compilationDto.setEvents(eventShortDtos);
        return compilationDto;
    }
}
