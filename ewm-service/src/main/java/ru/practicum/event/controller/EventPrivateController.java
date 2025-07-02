package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventPrivateService;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class EventPrivateController {

    private final EventPrivateService eventPrivateService;

    @GetMapping
    public List<EventShortDto> getPrivateEvents(@PathVariable Long userId, @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        log.info("Запрос на получение событий, добавленных пользователем id = {} (private)", userId);
        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректные параметры пагинации");
        }
        return eventPrivateService.getAll(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@Valid @RequestBody NewEventDto newEventDto, @PathVariable Long userId) {
        log.info("Запрос на создание события пользователем id = {} (private)", userId);
        return eventPrivateService.create(newEventDto, userId);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByInitiatorId(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Запрос на получение события id = {}, добавленного пользователем id = {} (private)", eventId, userId);
        return eventPrivateService.getByInitiatorId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByInitiatorId(@Valid @RequestBody UpdateEventUserRequest request,
                                                 @PathVariable Long userId,
                                                 @PathVariable Long eventId) {
        log.info("Запрос на изменение события id = {}, добавленного пользователем id = {} (private)", eventId, userId);
        return eventPrivateService.update(request, userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Запрос на получение заявок на участие в событии id = {} пользователя id = {} (private)", eventId, userId);
        return eventPrivateService.getRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestUpdateStatusResult updateEventRequest(@RequestBody EventRequestStatusUpdateRequest request,
                                                             @PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Запрос на изменение статуса заявок на участие в событии id = {} пользователя id = {} (private)", eventId, userId);
        return eventPrivateService.updateRequest(request, userId, eventId);
    }
}
