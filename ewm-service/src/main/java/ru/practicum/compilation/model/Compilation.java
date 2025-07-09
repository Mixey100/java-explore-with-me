package ru.practicum.compilation.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.model.Event;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "compilations")
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @JoinTable(name = "compilations_events",
            joinColumns = @JoinColumn(name = "compilation_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "event_id", referencedColumnName = "id"))
    @ManyToMany
    List<Event> events;

    Boolean pinned;

    String title;
}
