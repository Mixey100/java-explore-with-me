package ru.practicum.request.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@Setter
@Table(name = "requests")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "event")
    Event event;

    @ManyToOne
    @JoinColumn(name = "requester")
    User requester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    RequestStatus status;

    @Column(nullable = false)
    LocalDateTime created;
}
