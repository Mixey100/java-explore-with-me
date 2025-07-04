package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;

import java.util.List;

public interface CommentPublicService {

    CommentDto getById(Long commentId);

    List<CommentDto> getByEventId(Long eventId);
}
