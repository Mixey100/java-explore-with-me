package ru.practicum.request.service;

import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getRequestsByRequesterId(Long requesterId);

    ParticipationRequestDto createRequest(Long requesterId, Long eventId);

    ParticipationRequestDto updateRequest(Long requesterId, Long requestId);
}
