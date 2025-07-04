package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentPublicService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/comments")
public class CommentPublicController {

    private final CommentPublicService commentPublicService;

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(@PathVariable Long commentId) {
        log.info("Запрос на получение комментария id = {} ()", commentId);
        return commentPublicService.getById(commentId);
    }

    @GetMapping("/events/{eventId}")
    public List<CommentDto> getComments(@PathVariable Long eventId) {
        log.info("Запрос на получение комментариев к событию id = {}", eventId);
        return commentPublicService.getByEventId(eventId);
    }
}
