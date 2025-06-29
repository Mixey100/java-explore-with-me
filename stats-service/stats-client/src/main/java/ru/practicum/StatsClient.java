package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class StatsClient {

    private final RestTemplate restTemplate;
    private final String serverUrl;

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplate rest) {
        this.restTemplate = rest;
        this.serverUrl = serverUrl;
    }

    public List<ViewStatsDto> getStats(StatsRequest request) {
        if (request == null || !request.isValid()) {
            log.warn("Некорректные параметры запроса статистики");
            return Collections.emptyList();
        }
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl)
                    .path("/stats")
                    .queryParam("start", request.getStart())
                    .queryParam("end", request.getEnd())
                    .queryParam("unique", request.getUnique());
            if (request.getUris() != null && !request.getUris().isEmpty()) {
                builder.queryParam("uris", String.join(",", request.getUris()));
            }
            ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
                    null, new ParameterizedTypeReference<>() {
                    });
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Ошибка при запросе статистики: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean hit(EndpointHitDtoRequest dto) {
        try {
            String uri = UriComponentsBuilder.fromHttpUrl(serverUrl)
                    .path("/hit")
                    .encode()
                    .toUriString();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<EndpointHitDtoRequest> entity = new HttpEntity<>(dto, headers);
            ResponseEntity<Void> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Неуспешный ответ при отправке hit: {}", response.getStatusCode());
                return false;
            }
            return true;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Ошибка при отправке hit: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Неожиданная ошибка: {}", e.getMessage());
            return false;
        }
    }
}


