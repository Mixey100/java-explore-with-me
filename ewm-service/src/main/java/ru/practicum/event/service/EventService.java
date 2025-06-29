package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.State;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShortDto> getPublicEvents(String text, List<Long> categoryIds, Boolean paid, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                        Integer size, HttpServletRequest httpServletRequest);

    EventFullDto getPublicEventById(Long eventId, HttpServletRequest httpServletRequest);

    List<EventFullDto> getAdminEvents(List<Long> userIds, List<State> states, List<Long> categoryIds,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateAdminEvent(UpdateEventAdminRequest request, Long eventId);

    List<EventShortDto> getPrivateEvents(Long userId, Integer from, Integer size);

    EventFullDto createPrivateEvent(NewEventDto newEventDto, Long userId);

    EventFullDto getPrivateEventByInitiatorId(Long userId, Long eventId);

    EventFullDto updatePrivateEventByInitiatorId(UpdateEventUserRequest request, Long userId, Long eventId);

    List<ParticipationRequestDto> getPrivateEventRequests(Long userId, Long eventId);

    EventRequestUpdateStatusResult updatePrivateEventRequest(EventRequestStatusUpdateRequest request, Long userId,
                                                             Long eventId);
}
