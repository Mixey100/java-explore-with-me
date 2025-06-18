package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDtoRequest;
import ru.practicum.ViewStatsDto;
import ru.practicum.ViewStatsProjection;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EndpointMapper;
import ru.practicum.storage.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public void create(EndpointHitDtoRequest dtoRequest) {
        log.info("Сохранение статистики");
        statsRepository.save(EndpointMapper.mapToEntity(dtoRequest));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("Получение статистики");
        if (start.isAfter(end)) {
            throw new ValidationException("Начальная дата не может быть позже конечной");
        }
        List<ViewStatsProjection> result;
        if (uris == null || uris.isEmpty()) {
            if (!unique) {
                result = statsRepository.findAllNotUrisAndNotUnique(start, end);
            } else {
                result = statsRepository.findAllNotUrisAndUnique(start, end);
            }
        } else {
            if (!unique) {
                result = statsRepository.findAllWithUrisAndNotUnique(start, end, uris);
            } else {
                result = statsRepository.findAllWithUrisAndUnique(start, end, uris);
            }
        }
        return result.stream()
                .map(viewStats -> ViewStatsDto.builder()
                        .app(viewStats.getApp())
                        .uri(viewStats.getUri())
                        .hits(viewStats.getHits())
                        .build())
                .toList();
    }
}
