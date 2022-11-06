package ru.practicum.main_server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.client.HitClient;
import ru.practicum.main_server.dto.*;
import ru.practicum.main_server.exception.ObjectNotFoundException;
import ru.practicum.main_server.exception.WrongRequestException;
import ru.practicum.main_server.mapper.EventMapper;
import ru.practicum.main_server.model.*;
import ru.practicum.main_server.repository.CategoryRepository;
import ru.practicum.main_server.repository.EventRepository;
import ru.practicum.main_server.repository.ParticipationRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventService {
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final UserService userService;
    private final HitClient hitClient;
    private final CategoryRepository categoryRepository;
    private final LocationService locationService;


    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                         String rangeEnd, Boolean onlyAvailable, String sort, int from, int size) {
        LocalDateTime start = (rangeStart == null) ? LocalDateTime.now() :
                LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        LocalDateTime end;
        if (rangeEnd == null) {
            end = LocalDateTime.MAX;
        } else {
            end = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        List<Event> events = eventRepository.searchEvents(text, categories, paid, start, end,
                        PageRequest.of(from / size, size))
                .stream()
                .collect(Collectors.toList());
        if (sort.equals("EVENT_DATE")) {
            events = events.stream()
                    .sorted(Comparator.comparing(Event::getEventDate))
                    .collect(Collectors.toList());
        }

        List<EventShortDto> eventShortDtoList = events.stream()
                .filter(event -> event.getState().equals(State.PUBLISHED))
                .map(EventMapper::toEventShortDto)
                .map(this::setConfirmedRequestsAndViewsEventShortDto)
                .collect(Collectors.toList());
        if (sort.equals("VIEWS")) {
            eventShortDtoList = eventShortDtoList.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews))
                    .collect(Collectors.toList());
        }
        if (onlyAvailable) {
            eventShortDtoList = eventShortDtoList.stream()
                    .filter(eventShortDto -> eventShortDto.getConfirmedRequests()
                            <= checkAndGetEvent(eventShortDto.getId()).getParticipantLimit())
                    .collect(Collectors.toList());
        }
        return eventShortDtoList;
    }

    public EventResponseDto getEventById(long id) {
        EventResponseDto dto = EventMapper.toEventFullDto(checkAndGetEvent(id));
        if (!(dto.getState().equals(State.PUBLISHED.toString()))) {
            throw new WrongRequestException("Wrong state by request");
        }
        return setConfirmedRequestsAndViewsEventFullDto(dto);
    }

    public List<EventShortDto> getEventsCurrentUser(long userId, int from, int size) {
        return eventRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size))
                .stream()
                .map(EventMapper::toEventShortDto)
                .map(this::setConfirmedRequestsAndViewsEventShortDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventResponseDto updateEvent(Long userId, UpdateEventRequest updateEventRequest) {

        Event event = checkAndGetEvent(updateEventRequest.getEventId());
        if (!event.getInitiator().getId().equals(userId)) {
            throw new WrongRequestException("only creator can update event");
        }
        if (event.getState().equals(State.PUBLISHED)) {
            throw new WrongRequestException("you can`t update published event");
        }
        if (updateEventRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventRequest.getAnnotation());
        }
        if (updateEventRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventRequest.getCategory())
                    .orElseThrow(() -> new ObjectNotFoundException("Category not found"));
            event.setCategory(category);
        }
        if (updateEventRequest.getEventDate() != null) {
            LocalDateTime date = LocalDateTime.parse(updateEventRequest.getEventDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (date.isBefore(LocalDateTime.now().minusHours(2))) {
                throw new WrongRequestException("date event is too late");
            }
            event.setEventDate(date);
        }
        if (updateEventRequest.getDescription() != null) {
            event.setDescription(updateEventRequest.getDescription());
        }
        if (updateEventRequest.getPaid() != null) {
            event.setPaid(updateEventRequest.getPaid());
        }
        if (updateEventRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventRequest.getParticipantLimit());
        }
        if (updateEventRequest.getTitle() != null) {
            event.setTitle(updateEventRequest.getTitle());
        }
        event = eventRepository.save(event);
        EventResponseDto eventResponseDto = EventMapper.toEventFullDto(event);
        return setConfirmedRequestsAndViewsEventFullDto(eventResponseDto);
    }

    @Transactional
    public EventResponseDto createEvent(Long userId, EventRequestDto eventRequestDto) {
        Location location = eventRequestDto.getLocation();
        location = locationService.save(location);
        Event event = EventMapper.toNewEvent(eventRequestDto);
        if (event.getEventDate().isBefore(LocalDateTime.now().minusHours(2))) {
            throw new WrongRequestException("date event is too late");
        }
        event.setInitiator(userService.checkAndGetUser(userId));
        Category category = categoryRepository.findById(eventRequestDto.getCategory())
                .orElseThrow(() -> new ObjectNotFoundException("Category not found"));
        event.setCategory(category);
        event.setLocation(location);

        event = eventRepository.save(event);
        EventResponseDto eventResponseDto = EventMapper.toEventFullDto(event);
        return setConfirmedRequestsAndViewsEventFullDto(eventResponseDto);
    }

    public EventResponseDto getEventCurrentUser(Long userId, Long eventId) {
        Event event = checkAndGetEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new WrongRequestException("only initiator can get fullEventDto");
        }
        return setConfirmedRequestsAndViewsEventFullDto(EventMapper.toEventFullDto(event));
    }

    @Transactional
    public EventResponseDto cancelEvent(Long userId, Long eventId) {
        Event event = checkAndGetEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new WrongRequestException("only initiator can cancel event");
        }
        if (!event.getState().equals(State.PENDING)) {
            throw new WrongRequestException("you can cancel only pending event");
        }
        event.setState(State.CANCELED);
        event = eventRepository.save(event);
        EventResponseDto eventResponseDto = EventMapper.toEventFullDto(event);
        return setConfirmedRequestsAndViewsEventFullDto(eventResponseDto);
    }


    public List<EventResponseDto> getAdminEvents(List<Long> users, List<State> states, List<Long> categories,
                                                 String rangeStart, String rangeEnd, int from, int size) {
        LocalDateTime start;
        if (rangeStart == null) {
            start = LocalDateTime.now();
        } else {
            start = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        LocalDateTime end;
        if (rangeEnd == null) {
            end = LocalDateTime.MAX;
        } else {
            end = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        return eventRepository.searchEventsByAdmin(users, states, categories, start, end,
                        PageRequest.of(from / size, size))
                .stream()
                .map(EventMapper::toEventFullDto)
                .map(this::setConfirmedRequestsAndViewsEventFullDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventResponseDto updateEventByAdmin(Long eventId, AdminUpdateEventRequest adminUpdateEventRequest) {
        Event event = checkAndGetEvent(eventId);

        Optional.ofNullable(adminUpdateEventRequest.getAnnotation())
                .ifPresent(event::setAnnotation);

        if (adminUpdateEventRequest.getCategory() != null) {
            Category category = categoryRepository.findById(adminUpdateEventRequest.getCategory())
                    .orElseThrow(() -> new ObjectNotFoundException("Category not found"));
            event.setCategory(category);
        }

        Optional.ofNullable(adminUpdateEventRequest.getDescription())
                .ifPresent(event::setDescription);


        if (adminUpdateEventRequest.getEventDate() != null) {
            LocalDateTime date = LocalDateTime.parse(adminUpdateEventRequest.getEventDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (date.isBefore(LocalDateTime.now().minusHours(2))) {
                throw new WrongRequestException("date event is too late");
            }
            event.setEventDate(date);
        }
        Optional.ofNullable(adminUpdateEventRequest.getLocation())
                .ifPresent(event::setLocation);

        Optional.ofNullable(adminUpdateEventRequest.getRequestModeration())
                .ifPresent(event::setRequestModeration);

        Optional.ofNullable(adminUpdateEventRequest.getPaid())
                .ifPresent(event::setPaid);

        Optional.ofNullable(adminUpdateEventRequest.getParticipantLimit())
                .ifPresent(event::setParticipantLimit);

        Optional.ofNullable(adminUpdateEventRequest.getTitle())
                .ifPresent(event::setTitle);

        event = eventRepository.save(event);
        EventResponseDto eventResponseDto = EventMapper.toEventFullDto(event);
        return setConfirmedRequestsAndViewsEventFullDto(eventResponseDto);
    }

    @Transactional
    public EventResponseDto publishEventByAdmin(Long eventId) {
        Event event = checkAndGetEvent(eventId);
        if (event.getEventDate().isBefore(LocalDateTime.now().minusHours(2))) {
            throw new WrongRequestException("date event is too late");
        }
        if (!event.getState().equals(State.PENDING)) {
            throw new WrongRequestException("admin can publish only pending event");
        }
        event.setState(State.PUBLISHED);
        event = eventRepository.save(event);
        EventResponseDto eventResponseDto = EventMapper.toEventFullDto(event);
        return setConfirmedRequestsAndViewsEventFullDto(eventResponseDto);
    }

    @Transactional
    public EventResponseDto rejectEventByAdmin(Long eventId) {
        Event event = checkAndGetEvent(eventId);

        event.setState(State.CANCELED);
        event = eventRepository.save(event);
        EventResponseDto eventResponseDto = EventMapper.toEventFullDto(event);
        return setConfirmedRequestsAndViewsEventFullDto(eventResponseDto);
    }

    public Event checkAndGetEvent(long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new ObjectNotFoundException("event with id = " + eventId + " not found"));
    }

    public EventShortDto setConfirmedRequestsAndViewsEventShortDto(EventShortDto eventShortDto) {
        int confirmedRequests = participationRepository
                .countByEventIdAndStatus(eventShortDto.getId(), StatusRequest.CONFIRMED);
        eventShortDto.setConfirmedRequests(confirmedRequests);
        eventShortDto.setViews(getViews(eventShortDto.getId()));
        return eventShortDto;
    }

    public EventResponseDto setConfirmedRequestsAndViewsEventFullDto(EventResponseDto eventResponseDto) {
        int confirmedRequests = participationRepository
                .countByEventIdAndStatus(eventResponseDto.getId(), StatusRequest.CONFIRMED);
        eventResponseDto.setConfirmedRequests(confirmedRequests);
        eventResponseDto.setViews(getViews(eventResponseDto.getId()));
        return eventResponseDto;
    }

    public int getViews(long eventId) {
        ResponseEntity<Object> responseEntity = hitClient.getStat(
                LocalDateTime.of(2020, 9, 1, 0, 0),
                LocalDateTime.now(),
                List.of("/events/" + eventId),
                false);
        if (Objects.requireNonNull(responseEntity.getBody()).equals("")) {
            return (Integer) ((LinkedHashMap<?, ?>) responseEntity.getBody()).get("hits");
        }

        return 0;
    }

    public void sentHitStat(HttpServletRequest request) {
        EndpointHit endpointHit = EndpointHit.builder()
                .app("main_server")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
        hitClient.createHit(endpointHit);
    }
}
