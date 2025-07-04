package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoRequest;

import java.util.List;

public interface CommentPrivateService {

    List<CommentDto> getAllByEventIdAndAuthorId(Long eventId, Long authorId);

    CommentDto create(CommentDtoRequest request, Long eventId, Long authorId);

    CommentDto update(CommentDtoRequest request, Long commentId, Long evenId, Long authorId);

    void delete(Long commentId, Long eventId, Long authorId);
}
