package ru.practicum.comment.service;

import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.comment.model.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class DbCommentSpecification {

    public Specification<Comment> getAdminSpecification(List<Long> events, List<Long> authors, LocalDateTime rangeStart,
                                                        LocalDateTime rangeEnd) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (events != null && !events.isEmpty()) {
                predicates.add(root.get("event").get("id").in(events));
            }
            if (authors != null && !authors.isEmpty()) {
                predicates.add(root.get("author").get("id").in(authors));
            }
            if (rangeStart != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("created"), rangeStart));
            }
            if (rangeEnd != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("created"), rangeEnd));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
