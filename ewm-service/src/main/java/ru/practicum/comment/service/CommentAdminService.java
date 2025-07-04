package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentAdminFilter;
import ru.practicum.comment.dto.CommentDto;

import java.util.List;

public interface CommentAdminService {

    List<CommentDto> getAll(CommentAdminFilter commentAdminFilter, Integer from, Integer size);

    void delete(Long commentId);
}
