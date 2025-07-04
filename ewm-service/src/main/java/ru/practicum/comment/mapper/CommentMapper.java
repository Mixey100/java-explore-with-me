package ru.practicum.comment.mapper;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoRequest;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class CommentMapper {

    public static Comment mapToComment(CommentDtoRequest request, Event event, User author) {
        return Comment.builder()
                .text(request.getText())
                .event(event)
                .author(author)
                .created(LocalDateTime.now())
                .build();
    }

    public static CommentDto mapToCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .event(EventMapper.mapToEventShortDto(comment.getEvent()))
                .author(UserMapper.mapToUserShotDto(comment.getAuthor()))
                .created(comment.getCreated())
                .build();
    }

    public static List<CommentDto> MapToCommentDto(List<Comment> comments) {
        return comments.stream()
                .map(CommentMapper::mapToCommentDto)
                .toList();
    }
}

