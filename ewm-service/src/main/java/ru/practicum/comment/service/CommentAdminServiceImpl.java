package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.comment.dto.CommentAdminFilter;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentAdminServiceImpl implements CommentAdminService {

    private final CommentRepository commentRepository;

    @Override
    public List<CommentDto> getAll(CommentAdminFilter commentAdminFilter, Integer from, Integer size) {
        commentAdminFilter.validateDates();
        Specification<Comment> specification = DbCommentSpecification.getAdminSpecification(
                commentAdminFilter.getEventIds(),
                commentAdminFilter.getAuthorIds(),
                commentAdminFilter.getRangeStart(),
                commentAdminFilter.getRangeEnd());
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAll(specification, pageable).getContent();
        return CommentMapper.mapToCommentDto(comments);
    }

    @Override
    public void delete(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Комментарий id = %d не найден".formatted(commentId));
        }
        commentRepository.deleteById(commentId);
    }
}
