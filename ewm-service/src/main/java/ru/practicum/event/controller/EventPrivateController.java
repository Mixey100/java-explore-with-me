package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class EventPrivateController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getPrivateEvents(@PathVariable Long userId, @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        return eventService.getPrivateEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createPrivateEvent(@Valid @RequestBody NewEventDto newEventDto, @PathVariable Long userId) {
        return eventService.createPrivateEvent(newEventDto, userId);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getPrivateEventByInitiatorId(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getPrivateEventByInitiatorId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updatePrivateEventByInitiatorId(@Valid @RequestBody UpdateEventUserRequest request,
                                                        @PathVariable Long userId,
                                                        @PathVariable Long eventId) {
        return eventService.updatePrivateEventByInitiatorId(request, userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getPrivateEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getPrivateEventRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestUpdateStatusResult updatePrivateEventRequest(@RequestBody EventRequestStatusUpdateRequest request,
                                                                    @PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.updatePrivateEventRequest(request, userId, eventId);
    }
}
