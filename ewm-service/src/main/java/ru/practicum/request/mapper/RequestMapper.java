package ru.practicum.request.mapper;

import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;

public class RequestMapper {

    public static ParticipationRequestDto mapToRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .eventId(request.getEvent().getId())
                .requesterId(request.getRequester().getId())
                .status(request.getStatus())
                .created(request.getCreated())
                .build();
    }
}
