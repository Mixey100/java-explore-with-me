package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.repository.EventRepository;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getRequestsByRequesterId(Long requesterId) {
        List<Request> requests = requestRepository.findAllByRequesterId(requesterId);
        return requests.stream()
                .map(RequestMapper::mapToRequestDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createRequest(Long requesterId, Long eventId) {
        User requester = userRepository.findById(requesterId).orElseThrow(() ->
                new NotFoundException("Пользователь с Id " + requesterId + " не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с Id " + requesterId + " не найдено"));
        if (requestRepository.existsByRequesterIdAndEventId(requesterId, eventId)) {
            throw new ConflictException("Нельзя добавить повторный запрос");
        }
        if (event.getInitiator().getId().equals(requesterId)) {
            throw new ConflictException("Нельзя добавить запрос на участие в своем событии");
        }
        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("Нельзя добавить запрос на участие в неопубликованном событии");
        }
        if (event.getParticipantLimit() > 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит запросов на участие");
        }
        Request request = new Request();
        if (event.getRequestModeration() && event.getParticipantLimit() > 0) {
            request.setStatus(RequestStatus.PENDING);
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
        }
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(requester);
        Request savedRequest = requestRepository.save(request);
        if (request.getStatus() == RequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        return RequestMapper.mapToRequestDto(savedRequest);
    }

    @Override
    public ParticipationRequestDto updateRequest(Long requesterId, Long requestId) {
        log.info("Запрос на отмeну запроса {} на участие в событии", requestId);
        if (!userRepository.existsById(requesterId)) {
            throw new NotFoundException("Пользователь с Id " + requesterId + " не найден");
        }
        Request request = requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос с Id " + requestId + " не найден"));
        if (!request.getRequester().getId().equals(requesterId)) {
            throw new ValidationException("Пользователь " + requesterId + " не создавал запрос " + requestId);
        }
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.mapToRequestDto(requestRepository.save(request));
    }
}
