package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDtoRequest;
import ru.practicum.StatsRequest;
import ru.practicum.ViewStatsDto;
import ru.practicum.ViewStatsProjection;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EndpointMapper;
import ru.practicum.storage.StatsRepository;

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
    public List<ViewStatsDto> getStats(StatsRequest request) {
        log.info("Получение статистики");
        if (request == null) {
            throw new ValidationException("StatsRequest не должен быть null");
        }
        if (request.getStart() == null || request.getEnd() == null) {
            throw new ValidationException("Начальная дата и конечная дата обязательны");
        }
        if (request.getStart().isAfter(request.getEnd())) {
            throw new ValidationException("Начальная дата не может быть позже конечной");
        }
        List<ViewStatsProjection> result;
        if (request.getUris() == null || request.getUris().isEmpty()) {
            if (!request.getUnique()) {
                result = statsRepository.findAllNotUrisAndNotUnique(request.getStart(), request.getEnd());
            } else {
                result = statsRepository.findAllNotUrisAndUnique(request.getStart(), request.getEnd());
            }
        } else {
            if (!request.getUnique()) {
                result = statsRepository.findAllWithUrisAndNotUnique(request.getStart(), request.getEnd(), request.getUris());
            } else {
                result = statsRepository.findAllWithUrisAndUnique(request.getStart(), request.getEnd(), request.getUris());
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
