package ru.practicum.service;

import ru.practicum.EndpointHitDtoRequest;
import ru.practicum.StatsRequest;
import ru.practicum.ViewStatsDto;

import java.util.List;

public interface StatsService {

    void create(EndpointHitDtoRequest dtoRequest);

    List<ViewStatsDto> getStats(StatsRequest request);
}
