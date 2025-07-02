package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/requests")
public class RequestController {

    private final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getRequestsByRequesterId(@PathVariable Long userId) {
        log.info("Запрос информации о заявках пользователя id = {} на участие в событиях", userId);
        return requestService.getByRequesterId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("Запрос пользователя id = {} на участие в событии id = {}", userId, eventId);
        return requestService.create(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto updateRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("Запрос пользователя {} на отмeну участия в событии {}", userId, requestId);
        return requestService.update(userId, requestId);
    }
}
