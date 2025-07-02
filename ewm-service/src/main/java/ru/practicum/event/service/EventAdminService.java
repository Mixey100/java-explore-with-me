package ru.practicum.event.service;

import ru.practicum.event.dto.EventAdminFilter;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;

import java.util.List;

public interface EventAdminService {

    List<EventFullDto> getAll(EventAdminFilter adminFilter, Integer from, Integer size);

    EventFullDto update(UpdateEventAdminRequest request, Long eventId);
}
