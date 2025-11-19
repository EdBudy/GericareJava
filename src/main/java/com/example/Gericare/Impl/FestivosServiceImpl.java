package com.example.Gericare.Impl;

import com.example.Gericare.Service.FestivosService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class FestivosServiceImpl implements FestivosService {

    private final Map<Integer, List<LocalDate>> cacheFestivos = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_URL = "https://date.nager.at/api/v3/PublicHolidays/{year}/CO";

    @Override
    public boolean esFestivo(LocalDate fecha) {
        if (fecha == null) return false;
        int anio = fecha.getYear();

        List<LocalDate> festivosDelAnio = cacheFestivos.computeIfAbsent(anio, this::consultarApiFestivos);

        return festivosDelAnio.contains(fecha);
    }

    private List<LocalDate> consultarApiFestivos(int anio) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0"); // Necesario para que la API no bloquee
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {},
                    anio
            );

            if (response.getBody() != null) {
                return response.getBody().stream()
                        .map(festivo -> LocalDate.parse((String) festivo.get("date")))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Error consultando API de festivos: " + e.getMessage());
        }
        return Collections.emptyList();
    }
}