package ru.practicum.mapper;

import ru.practicum.EndpointHitDtoRequest;
import ru.practicum.model.EndpointHit;

public class EndpointMapper {
    public static EndpointHit mapToEntity(EndpointHitDtoRequest request) {
        EndpointHit endpointHit = EndpointHit.builder()
                .app(request.getApp())
                .uri(request.getUri())
                .ip(request.getIp())
                .timestamp(request.getTimestamp())
                .build();
        return endpointHit;
    }
}
