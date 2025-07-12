package ru.practicum.comment.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentAdminFilter {
    List<Long> eventIds;
    List<Long> authorIds;
    LocalDateTime rangeStart;
    LocalDateTime rangeEnd;

    public void validateDates() {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата начала должна быть раньше даты окончания");
        }
    }
}
