package ru.practicum.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoRequest;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentPrivateServiceImpl implements CommentPrivateService {

    final CommentRepository commentRepository;
    final UserRepository userRepository;
    final EventRepository eventRepository;

    @Override
    public List<CommentDto> getAllByEventIdAndAuthorId(Long eventId, Long authorId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие id = %d не найдено".formatted(eventId));
        }
        if (!userRepository.existsById(authorId)) {
            throw new NotFoundException("Пользователь id = %d не найден".formatted(authorId));
        }
        List<Comment> comments = commentRepository.findAllByEventIdAndAuthorId(eventId, authorId);
        return CommentMapper.mapToCommentDto(comments);
    }

    @Override
    public CommentDto create(CommentDtoRequest request, Long eventId, Long authorId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие id = %d не найдено".formatted(eventId)));
        if (event.getState() != State.PUBLISHED) {
            throw new ValidationException("Событие id = %d не опубликовано".formatted(eventId));
        }
        User author = userRepository.findById(authorId).orElseThrow(() ->
                new NotFoundException("Пользователь id = %d не найден".formatted(authorId)));
        Comment comment = commentRepository.save(CommentMapper.mapToComment(request, event, author));
        return CommentMapper.mapToCommentDto(comment);
    }

    @Override
    public CommentDto update(CommentDtoRequest request, Long commentId, Long evenId, Long authorId) {
        Comment comment = checkParams(commentId, evenId, authorId);
        comment.setText(request.getText());
        return CommentMapper.mapToCommentDto(commentRepository.save(comment));
    }

    @Override
    public void delete(Long commentId, Long eventId, Long authorId) {
        checkParams(commentId, eventId, authorId);
        commentRepository.deleteById(commentId);
    }

    private Comment checkParams(Long commentId, Long eventId, Long authorId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие id = %d не найдено".formatted(eventId));
        }
        if (!userRepository.existsById(authorId)) {
            throw new NotFoundException("Пользователь id = %d не найден".formatted(authorId));
        }
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Комментарий id = %d не найден".formatted(commentId)));
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new ConflictException("Пользователь id = %d не является автором комментария id = %d"
                    .formatted(authorId, commentId));
        }
        return comment;
    }
}
