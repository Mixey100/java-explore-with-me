package ru.practicum.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentPublicServiceImpl implements CommentPublicService {

    final CommentRepository commentRepository;
    final EventRepository eventRepository;

    @Override
    public CommentDto getById(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Комментарий id = %d не найден".formatted(commentId)));
        return CommentMapper.mapToCommentDto(comment);
    }

    @Override
    public List<CommentDto> getByEventId(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие id = %d не найдено".formatted(eventId));
        }
        List<Comment> comments = commentRepository.findAllByEventId(eventId);
        return CommentMapper.mapToCommentDto(comments);
    }
}
