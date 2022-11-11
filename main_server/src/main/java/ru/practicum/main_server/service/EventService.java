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
import ru.practicum.main_server.mapper.CommentMapper;
import ru.practicum.main_server.mapper.EventMapper;
import ru.practicum.main_server.model.*;
import ru.practicum.main_server.repository.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final UserService userService;
    private final HitClient hitClient;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;
    private final LocationService locationService;


    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                         String rangeEnd, Boolean onlyAvailable, String sort, int from, int size) {
        LocalDateTime start = (rangeStart == null) ? LocalDateTime.now() :
                LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER);

        LocalDateTime end;
        if (rangeEnd == null) {
            end = LocalDateTime.now().plusYears(2);
        } else {
            end = LocalDateTime.parse(rangeEnd, DATE_TIME_FORMATTER);
        }
        List<Event> events = new ArrayList<>();
        if (onlyAvailable) {
            if (sort.equals("EVENT_DATE")) {
                events = eventRepository.searchEventsAvailableOrderByEventDate(text, categories, paid, start,
                        end, PageRequest.of(from / size, size));
            } else if (sort.equals("VIEWS")) {
                events = eventRepository.searchEventsAvailableOrderByViews(text, categories, paid, start,
                        end, PageRequest.of(from / size, size));
            }
        } else {
            if (sort.equals("EVENT_DATE")) {
                events = eventRepository.searchEventsAllOrderByEventDate(text, categories, paid, start,
                        end, PageRequest.of(from / size, size));
            } else if (sort.equals("VIEWS")) {
                events = eventRepository.searchEventsAllOrderByViews(text, categories, paid, start,
                        end, PageRequest.of(from / size, size));
            }
        }
        return events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    public EventResponseDto getEventById(long id) {
        Event event = eventRepository.findById(id).orElseThrow(() ->
                new ObjectNotFoundException("Event not found"));
        if (!(event.getState().equals(State.PUBLISHED))) {
            throw new WrongRequestException("Wrong state by request");
        }
        setComments(event);
        setViewsEvent(event);
        return EventMapper.toEventFullDto(event);
    }

    public List<EventShortDto> getEventsCurrentUser(long userId, int from, int size) {
        return eventRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size))
                .stream()
                .map(EventMapper::toEventShortDto)
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
                    DATE_TIME_FORMATTER);
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
        setComments(event);
        return EventMapper.toEventFullDto(event);
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
        setComments(event);
        return EventMapper.toEventFullDto(event);
    }

    public EventResponseDto getEventCurrentUser(Long userId, Long eventId) {
        Event event = checkAndGetEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new WrongRequestException("only initiator can get fullEventDto");
        }
        setComments(event);
        return EventMapper.toEventFullDto(event);
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
        setComments(event);
        return EventMapper.toEventFullDto(event);
    }


    public List<EventResponseDto> getAdminEvents(List<Long> users, List<State> states, List<Long> categories,
                                                 String rangeStart, String rangeEnd, int from, int size) {
        LocalDateTime start;
        if (rangeStart == null) {
            start = LocalDateTime.now();
        } else {
            start = LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER);
        }
        LocalDateTime end;
        if (rangeEnd == null) {
            end = LocalDateTime.now().plusYears(2);
        } else {
            end = LocalDateTime.parse(rangeEnd, DATE_TIME_FORMATTER);
        }
        List<Event> events = eventRepository.searchEventsByAdmin(users, states, categories, start, end,
                PageRequest.of(from / size, size));
        for (Event event : events) {
            setComments(event);
        }
        return events.stream()
                .map(EventMapper::toEventFullDto)
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
                    DATE_TIME_FORMATTER);
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
        setComments(event);
        return EventMapper.toEventFullDto(event);
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
        setComments(event);
        return EventMapper.toEventFullDto(event);
    }

    @Transactional
    public EventResponseDto rejectEventByAdmin(Long eventId) {
        Event event = checkAndGetEvent(eventId);
        event.setState(State.CANCELED);
        setComments(event);
        return EventMapper.toEventFullDto(event);
    }

    public Event checkAndGetEvent(long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new ObjectNotFoundException("event with id = " + eventId + " not found"));
    }

    public void sentHitStat(HttpServletRequest request) {
        EndpointHit endpointHit = EndpointHit.builder()
                .app("main_server")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(DATE_TIME_FORMATTER))
                .build();
        hitClient.createHit(endpointHit);
    }

    @Transactional
    public Comment addComment(long userId, long eventId, CommentDto commentDto) {
        return commentRepository.save(CommentMapper.toComment(commentDto,
                userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException("User not found")),
                eventRepository.findById(eventId).orElseThrow(() -> new ObjectNotFoundException("Event not found"))));
    }

    public List<CommentDto> getCommentsByEvent(long eventId) {
        return commentRepository.findAllByEvent_Id(eventId).orElseThrow(() ->
                        new ObjectNotFoundException("Comments not found"))
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Comment updateCommentByUser(long userId, long eventId, long commentId, CommentDto commentDto) {
        Comment oldComment = commentRepository.findById(commentId).orElseThrow(() ->
                new ObjectNotFoundException("Comment not found"));
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ObjectNotFoundException("User not found"));
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new ObjectNotFoundException("Event not found"));
        if (userId != oldComment.getAuthor().getId()) {
            throw new WrongRequestException("Only creator can moderate his comments");
        }
        Comment updated = CommentMapper.toComment(commentDto, user, event);
        updated.setId(oldComment.getId());
        updated.setAuthor(oldComment.getAuthor());
        updated.setEvent(oldComment.getEvent());
        updated.setCreated(oldComment.getCreated());
        return commentRepository.save(updated);
    }

    @Transactional
    public Comment updateCommentByAdmin(long eventId, long commentId, CommentDto commentDto) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new ObjectNotFoundException("Comment not found"));
        if (eventRepository.findById(eventId).isPresent()) {
            comment.setText(commentDto.getText());
        } else {
            throw new ObjectNotFoundException("Event not found");
        }
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(long eventId, long commentId) {
        if (eventRepository.findById(eventId).isPresent()) {
            if (commentRepository.findById(commentId).isPresent()) {
                commentRepository.deleteById(commentId);
            } else {
                throw new ObjectNotFoundException("Comment not found");
            }
        } else {
            throw new ObjectNotFoundException("Event not found");
        }
    }

    private void setComments(Event event) {
        if (commentRepository.findAllByEvent_Id(event.getId()).isPresent()) {
            event.setComments(new ArrayList<>(commentRepository.findAllByEvent_Id(event.getId()).get()));
        } else {
            event.setComments(Collections.emptyList());
        }
    }

    @Transactional
    public void setViewsEvent(Event event) {
        event.setViews(getViews(event.getId()));
        eventRepository.save(event);
    }

    public long getViews(long eventId) {
        ResponseEntity<Object> responseEntity = hitClient.getStat(
                LocalDateTime.now().minusYears(2),
                LocalDateTime.now(),
                List.of("/events/" + eventId),
                false);
        if (Objects.equals(responseEntity.getBody(), "")) {
            return (Long) ((LinkedHashMap<?, ?>) responseEntity.getBody()).get("hits");
        }
        return 0;
    }

}
