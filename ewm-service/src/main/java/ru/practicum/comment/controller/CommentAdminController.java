package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentAdminFilter;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentAdminService;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/comments")
public class CommentAdminController {

    private final CommentAdminService commentAdminService;

    @GetMapping
    public List<CommentDto> getComments(@RequestParam(required = false) List<Long> eventIds,
                                        @RequestParam(required = false) List<Long> authorIds,
                                        @RequestParam(required = false)
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                        @RequestParam(required = false)
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                        @RequestParam(defaultValue = "0") Integer from,
                                        @RequestParam(defaultValue = "10") Integer size) {
        log.info("Запрос на получение комментариев с фильтрацией");
        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректные параметры пагинации");
        }
        CommentAdminFilter filter = CommentAdminFilter.builder()
                .eventIds(eventIds)
                .authorIds(authorIds)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .build();
        return commentAdminService.getAll(filter, from, size);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        log.info("Запрос на удаление комментария id = {}", commentId);
        commentAdminService.delete(commentId);
    }
}
