package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoRequest;
import ru.practicum.comment.service.CommentPrivateService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/events/{eventId}/comments")
public class CommentPrivateController {

    private final CommentPrivateService commentPrivateService;

    @GetMapping
    public List<CommentDto> getCommentsByEventIdAndUserId(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Запрос на получение комментариев пользователя id = {} к событию id = {}", userId, eventId);
        return commentPrivateService.getAllByEventIdAndAuthorId(eventId, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@Valid @RequestBody CommentDtoRequest request, @PathVariable Long userId,
                                    @PathVariable Long eventId) {
        log.info("Запрос на создание комментария к событию id = {} пользователем id = {}", eventId, userId);
        return commentPrivateService.create(request, eventId, userId);
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@Valid @RequestBody CommentDtoRequest request, @PathVariable Long userId,
                                    @PathVariable Long eventId, @PathVariable Long commentId) {
        log.info("Запрос на изменение комментария к событию id = {} пользователем id = {}", eventId, userId);
        return commentPrivateService.update(request, commentId, eventId, userId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId, @PathVariable Long eventId, @PathVariable Long commentId) {
        log.info("Запрос на удаление комментария id = {} пользователем id = {}", commentId, userId);
        commentPrivateService.delete(commentId, eventId, userId);
    }
}
