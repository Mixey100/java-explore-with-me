package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventPublicFilter;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

public interface EventPublicService {

    List<EventShortDto> getAll(EventPublicFilter publicFilter, Integer from, Integer size,
                               HttpServletRequest httpServletRequest);

    EventFullDto getById(Long eventId, HttpServletRequest httpServletRequest);
}
