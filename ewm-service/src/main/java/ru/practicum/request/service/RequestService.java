package ru.practicum.request.service;

import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getByRequesterId(Long requesterId);

    ParticipationRequestDto create(Long requesterId, Long eventId);

    ParticipationRequestDto update(Long requesterId, Long requestId);
}
