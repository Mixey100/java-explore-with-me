package ru.practicum.event.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.EventAdminFilter;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.model.StateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional(readOnly = true)
public class EventAdminServiceImpl implements EventAdminService {

    final EventRepository eventRepository;
    final CategoryRepository categoryRepository;
    final LocationRepository locationRepository;

    @Override
    public List<EventFullDto> getAll(EventAdminFilter adminFilter, Integer from, Integer size) {
        Specification<Event> specification = DbSpecification.getAdminSpecification(
                adminFilter.getUserIds(),
                adminFilter.getStates(),
                adminFilter.getCategoryIds(),
                adminFilter.getRangeStart(),
                adminFilter.getRangeEnd());
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAll(specification, pageable).getContent();
        return events.stream()
                .map(EventMapper::mapToEventFullDto)
                .sorted(Comparator.comparingLong(EventFullDto::getId).reversed())
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto update(UpdateEventAdminRequest request, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id = %d не найдено".formatted(eventId)));
        if (request.getAnnotation() != null && !request.getAnnotation().isBlank()) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategoryId() != null) {
            event.setCategory(categoryRepository.findById(request.getCategoryId()).orElseThrow(() ->
                    new NotFoundException("Категория с id = %d не найдена".formatted(request.getCategoryId()))));
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
                    throw new ConflictException("Событие id = %d уже опубликовано".formatted(event.getId()));
                } else if (event.getState() == State.REJECT) {
                    throw new ConflictException("Событие id = %d отменено".formatted(event.getId()));
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
}
