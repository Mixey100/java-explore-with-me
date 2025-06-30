package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDtoRequest;
import ru.practicum.StatsClient;
import ru.practicum.StatsRequest;
import ru.practicum.ViewStatsDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.*;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    final EventRepository eventRepository;
    final StatsClient statsClient;
    final CategoryRepository categoryRepository;
    final LocationRepository locationRepository;
    final UserRepository userRepository;
    final RequestRepository requestRepository;

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categoryIds, Boolean paid, LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                               Integer size, HttpServletRequest httpServletRequest) {
        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректные параметры пагинации");
        }
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Время начала события позже времени окончания");
        }
        Specification<Event> specification = DbSpecification.getPublicSpecification(text, categoryIds, paid, rangeStart,
                rangeEnd, onlyAvailable);
        EventSort eventSort = sort != null ? EventSort.valueOf(sort.toUpperCase()) : null;
        Sort sorting = Sort.unsorted();
        if (eventSort != null) {
            if (eventSort == EventSort.EVENT_DATE) {
                sorting = Sort.by(Sort.Direction.DESC, "eventDate");
            } else if (eventSort == EventSort.VIEWS) {
                sorting = Sort.by(Sort.Direction.DESC, "views");
            }
        }
        hit(httpServletRequest);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAll(specification, pageable).getContent();
        return EventMapper.mapToEventShortDto(events);
    }

    @Override
    public EventFullDto getPublicEventById(Long eventId, HttpServletRequest httpServletRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с Id " + eventId + " не найдено"));
        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException("getPublicEventById: Событие " + eventId + " не опубликовано");
        }
        hit(httpServletRequest);
        StatsRequest statsRequest = StatsRequest.builder()
                .start(event.getPublishedOn())
                .end(LocalDateTime.now())
                .uris(List.of("/events/" + eventId))
                .unique(true)
                .build();
        List<ViewStatsDto> stats = statsClient.getStats(statsRequest);
        log.info("Метод getPublicEventById, длина списка stats: {}", stats.size());
        Long views = stats.isEmpty() ? 0L : stats.getFirst().getHits();
        event.setViews(views);
        log.info("Метод getPublicEventById, количество сохраняемых просмотров: {}", views);
        return EventMapper.mapToEventFullDto(event);
    }

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> userIds, List<State> states, List<Long> categoryIds,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                             Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректные параметры пагинации");
        }
        Specification<Event> specification = DbSpecification.getAdminSpecification(userIds, states, categoryIds,
                rangeStart, rangeEnd);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAll(specification, pageable).getContent();
        log.info("Cписок eventsIds = {}", events.stream().map(Event::getId).toList());
        return events.stream()
                .map(EventMapper::mapToEventFullDto)
                .sorted(Comparator.comparingLong(EventFullDto::getId).reversed())
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateAdminEvent(UpdateEventAdminRequest request, Long eventId) {
        log.info("Запрос на обновление события id = {} и его статуса (admin)", eventId);
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id = " + eventId + " не найдено"));
        if (request.getAnnotation() != null && !request.getAnnotation().isBlank()) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategoryId() != null) {
            event.setCategory(categoryRepository.findById(request.getCategoryId()).orElseThrow(() ->
                    new NotFoundException("Категория с id = " + request.getCategoryId() + " не найдена.")));
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(locationRepository.save(LocationMapper.mapToLocation(request.getLocation())));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            event.setTitle(request.getTitle());
        }
        if (request.getEventDate() != null) {
            setEventDate(event, request.getEventDate());
        }
        setState(event, request);
        return EventMapper.mapToEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getPrivateEvents(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable).getContent();
        return events.stream()
                .map(EventMapper::mapToEventShortDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto createPrivateEvent(NewEventDto newEventDto, Long userId) {
        log.info("Запрос на создание события пользователем с id = {} (private)", userId);
        if (LocalDateTime.parse(newEventDto.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .isBefore(LocalDateTime.now())) {
            throw new ValidationException("Указана дата начала события в прошлом");
        }
        Category category = categoryRepository.findById(newEventDto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Категория " + newEventDto.getCategoryId() + " не найдена"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с Id " + userId + " не найден"));
        Location location = locationRepository.save(LocationMapper.mapToLocation(newEventDto.getLocation()));
        Event event = EventMapper.mapToEvent(newEventDto, category, user);
        event.setCreatedOn(LocalDateTime.now());
        event.setLocation(location);
        event.setConfirmedRequests(0);
        event.setState(State.PENDING);
        Event savedEvent = eventRepository.save(event);
        log.info("Событие с id = {} создано", savedEvent.getId());
        return EventMapper.mapToEventFullDto(savedEvent);
    }

    @Override
    public EventFullDto getPrivateEventByInitiatorId(Long userId, Long eventId) {
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Событие " + eventId + " не найдено"));
        return EventMapper.mapToEventFullDto(event);
    }

    @Override
    public EventFullDto updatePrivateEventByInitiatorId(UpdateEventUserRequest request, Long userId, Long eventId) {
        log.info("Запрос на обновление события с id = {} пользователем с id = {} (private)", eventId, userId);
        Event event = checkUpdatePrivateEvent(userId, eventId);
        if (event.getState() == State.PUBLISHED) {
            throw new ConflictException("Событие не отменено и не в состоянии ожидания.");
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Время события указано раньше, чем через два часа от текущего момента");
        }
        if (request.getAnnotation() != null && !request.getAnnotation().isBlank()) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(categoryRepository.findById(request.getCategory().getId()).orElseThrow(() ->
                    new NotFoundException("Категория с id = " + request.getCategory().getId() + " не найдена.")));
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(locationRepository.save(LocationMapper.mapToLocation(request.getLocation())));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            if (request.getParticipantLimit() < 0) {
                throw new ValidationException("Нельзя установить отрицательное значение лимита");
            }
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            event.setTitle(request.getTitle());
        }
        if (request.getStateAction() == StateAction.CANCEL_REVIEW) {
            event.setState(State.CANCELED);
        }
        if (request.getEventDate() != null) {
            setEventDate(event, request.getEventDate());
        }
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
                case REJECT_EVENT -> event.setState(State.REJECT);
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case PUBLISH_EVENT -> event.setState(State.PUBLISHED);
            }
        }
        return EventMapper.mapToEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<ParticipationRequestDto> getPrivateEventRequests(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с Id " + userId + " не найден");
        }
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id = " + eventId + " не найдено."));

        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ValidationException("Пользователь " + userId + " не является создателем события " + eventId);
        }
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream()
                .map(RequestMapper::mapToRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestUpdateStatusResult updatePrivateEventRequest(EventRequestStatusUpdateRequest requestDto, Long userId,
                                                                    Long eventId) {
        log.info("Запрос на обновление статуса заявок на событие с id = {} пользователя с id = {} (private)", eventId, userId);
        Event event = checkUpdatePrivateEvent(userId, eventId);
        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("Событие не опубликовано");
        }
        if (event.getConfirmedRequests() != null) {
            if (RequestStatus.CONFIRMED.equals(requestDto.getStatus())
                    && event.getConfirmedRequests() >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит заявок");
            }
        }
        List<Request> requests = requestRepository.findAllById(requestDto.getRequestIds());
        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();
        requests.forEach(request -> {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Статус не в состоянии ожидания");
            }
            if (event.getConfirmedRequests() < event.getParticipantLimit() && requestDto.getStatus() == RequestStatus.CONFIRMED) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(request);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }
        });
        eventRepository.save(event);
        requestRepository.saveAll(requests);
        List<ParticipationRequestDto> confirmedList = confirmedRequests.stream()
                .map(RequestMapper::mapToRequestDto)
                .toList();
        List<ParticipationRequestDto> regectedList = rejectedRequests.stream()
                .map(RequestMapper::mapToRequestDto)
                .toList();
        return new EventRequestUpdateStatusResult(confirmedList, regectedList);
    }

    private void hit(HttpServletRequest httpServletRequest) {
        EndpointHitDtoRequest hitDtoRequest = new EndpointHitDtoRequest(
                "main-server",
                httpServletRequest.getRequestURI(),
                httpServletRequest.getRemoteAddr(),
                LocalDateTime.now()
        );
        statsClient.hit(hitDtoRequest);
    }

    private void setEventDate(Event event, String date) {
        if (date != null) {
            if (LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .isBefore(LocalDateTime.now())) {
                throw new ValidationException("Указанная дата уже наступила");
            }
            event.setEventDate(LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
    }

    private void setState(Event event, UpdateEventAdminRequest request) {
        if (request.getStateAction() != null) {
            if (request.getStateAction() == StateAction.PUBLISH_EVENT) {
                if (event.getState() == State.PUBLISHED) {
                    throw new ConflictException("Событие уже опубликовано");
                } else if (event.getState() == State.REJECT) {
                    throw new ConflictException("Событие отменено");
                }
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (request.getStateAction() == StateAction.REJECT_EVENT) {
                if (event.getState() == State.PUBLISHED) {
                    throw new ConflictException("Нельзя отменить опубликованное событие");
                }
                event.setState(State.REJECT);
            }
        }
    }

    private Event checkUpdatePrivateEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие " + eventId + " не найдено"));
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь " + userId + " не существует");
        }
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ValidationException("Пользователь " + userId + " не является создателем события " + eventId);
        }
        return event;
    }
}

