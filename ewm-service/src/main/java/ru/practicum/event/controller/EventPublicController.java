package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventPublicController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getPublicEvents(@RequestParam(required = false) @Size(min = 1, max = 7000) String text,
                                               @RequestParam(required = false) List<Long> categoryIds,
                                               @RequestParam(required = false) Boolean paid,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                               LocalDateTime rangeStart,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                               LocalDateTime rangeEnd,
                                               @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                               @RequestParam(required = false) String sort,
                                               @RequestParam(defaultValue = "0") @Min(value = 0) Integer from,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               HttpServletRequest httpServletRequest) {
        return eventService.getPublicEvents(text, categoryIds, paid, rangeStart,
                rangeEnd, onlyAvailable, sort, from, size, httpServletRequest);
    }

    @GetMapping("/{id}")
    public EventFullDto gePublicEventById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        return eventService.getPublicEventById(id, httpServletRequest);
    }
}