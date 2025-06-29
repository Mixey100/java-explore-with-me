package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректные параметры пагинации");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable).getContent();
        return compilations.stream()
                .map(CompilationMapper::mapToCompilationDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Подборка событий с id = " + compId + " не найдена"));
        return CompilationMapper.mapToCompilationDto(compilation);
    }

    @Override
    public CompilationDto createCompilation(NewCompilationDto compilationDto) {
        log.info("Запрос на создание подборки событий");
        if (compilationDto.getEvents() == null) {
            compilationDto.setEvents(new ArrayList<>());
        }
        List<Event> events = eventRepository.findAllById(compilationDto.getEvents());
        Compilation compilation = compilationRepository.save(CompilationMapper.mapToCompilation(compilationDto, events));
        return CompilationMapper.mapToCompilationDto(compilation);
    }

    @Override
    public CompilationDto updateCompilation(UpdateCompilationDto compilationDto, Long compId) {
        log.info("Запрос на обновление подборки событий id = {}", compId);
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Подборка событий с id = " + compId + " не найдена"));
        if (compilationDto.getEvents() == null) {
            compilationDto.setEvents(new ArrayList<>());
        }
        compilation.setPinned(compilationDto.getPinned());
        if (!compilationDto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(compilationDto.getEvents());
            compilation.setEvents(events);
        }
        if (compilationDto.getTitle() != null && !compilationDto.getTitle().isBlank()) {
            compilation.setTitle(compilationDto.getTitle());
        }
        return CompilationMapper.mapToCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public void deleteCompilation(Long compId) {
        log.info("Запрос на удаление подборки событий с id {}", compId);
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка событий с id = " + compId + " не найдена");
        }
        compilationRepository.deleteById(compId);
    }
}
