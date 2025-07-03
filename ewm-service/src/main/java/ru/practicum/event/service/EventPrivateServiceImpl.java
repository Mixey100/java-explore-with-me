package ru.practicum.event.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.State;
import ru.practicum.event.model.StateAction;
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
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventPrivateServiceImpl implements EventPrivateService {

    final EventRepository eventRepository;
    final CategoryRepository categoryRepository;
    final LocationRepository locationRepository;
    final UserRepository userRepository;
    final RequestRepository requestRepository;

    @Override
    public List<EventShortDto> getAll(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable).getContent();
        return events.stream()
                .map(EventMapper::mapToEventShortDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto create(NewEventDto newEventDto, Long userId) {
        if (LocalDateTime.parse(newEventDto.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .isBefore(LocalDateTime.now())) {
            throw new ValidationException("Указана дата начала события в прошлом");
        }
        Category category = categoryRepository.findById(newEventDto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Категория id = %d не найдена".formatted(newEventDto.getCategoryId())));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = %d не найден".formatted(userId)));
        Location location = locationRepository.save(LocationMapper.mapToLocation(newEventDto.getLocation()));
        Event event = EventMapper.mapToEvent(newEventDto, category, user);
        event.setCreatedOn(LocalDateTime.now());
        event.setLocation(location);
        event.setConfirmedRequests(0);
        event.setState(State.PENDING);
        Event savedEvent = eventRepository.save(event);
        return EventMapper.mapToEventFullDto(savedEvent);
    }

    @Override
    public EventFullDto getByInitiatorId(Long userId, Long eventId) {
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Событие id = %d не найдено".formatted(eventId)));
        return EventMapper.mapToEventFullDto(event);
    }

    @Override
    public EventFullDto update(UpdateEventUserRequest request, Long userId, Long eventId) {
        Event event = checkUpdateEvent(userId, eventId);
        if (event.getState() == State.PUBLISHED) {
            throw new ConflictException("Событие id = %d не отменено и не в состоянии ожидания.".formatted(eventId));
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Время события указано раньше, чем через два часа от текущего момента");
        }
        if (request.getAnnotation() != null && !request.getAnnotation().isBlank()) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(categoryRepository.findById(request.getCategory().getId()).orElseThrow(() ->
                    new NotFoundException("Категория с id = %d не найдена.".formatted(request.getCategory().getId()))));
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
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь id = %d не найден".formatted(userId));
        }
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие id = %d не найдено.".formatted(eventId)));

        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ValidationException("Пользователь id = %d не является создателем события id = %d".formatted(userId, eventId));
        }
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream()
                .map(RequestMapper::mapToRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestUpdateStatusResult updateRequest(EventRequestStatusUpdateRequest requestDto, Long userId,
                                                        Long eventId) {
        Event event = checkUpdateEvent(userId, eventId);
        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("Событие id = %d не опубликовано".formatted(eventId));
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

    private Event checkUpdateEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие id = %d не найдено".formatted(eventId)));
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь id = %d не существует".formatted(userId));
        }
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ValidationException("Пользователь id = %d не является создателем события id = %d".formatted(userId, eventId));
        }
        return event;
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
}
