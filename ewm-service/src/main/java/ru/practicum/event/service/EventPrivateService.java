package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface EventPrivateService {

    List<EventShortDto> getAll(Long userId, Integer from, Integer size);

    EventFullDto create(NewEventDto newEventDto, Long userId);

    EventFullDto getByInitiatorId(Long userId, Long eventId);

    EventFullDto update(UpdateEventUserRequest request, Long userId, Long eventId);

    List<ParticipationRequestDto> getRequests(Long userId, Long eventId);

    EventRequestUpdateStatusResult updateRequest(EventRequestStatusUpdateRequest request, Long userId,
                                                 Long eventId);
}
