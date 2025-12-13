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

    // Memoria temporal (Caché) para guardar festivos por año y evitar llamar a la API repetidamente.
    private final Map<Integer, List<LocalDate>> cacheFestivos = new ConcurrentHashMap<>();

    // Herramienta de Spring para realizar peticiones HTTP a servidores externos.
    private final RestTemplate restTemplate = new RestTemplate();

    // URL de la API pública Nager.Date configurada para Colombia (CO).
    private static final String API_URL = "https://date.nager.at/api/v3/PublicHolidays/{year}/CO";

    @Override
    public boolean esFestivo(LocalDate fecha) {
        // 1. Validación básica: si la fecha es nula, no puede ser festivo.
        if (fecha == null) return false;
        int anio = fecha.getYear();

        // 2. Optimización: Si ya consultamos este año antes, usa la memoria (cache).
        // Si no, llama al método consultarApiFestivos y guarda el resultado.
        List<LocalDate> festivosDelAnio = cacheFestivos.computeIfAbsent(anio, this::consultarApiFestivos);

        // 3. Verificación final: revisa si la fecha dada está en la lista obtenida.
        return festivosDelAnio.contains(fecha);
    }

    private List<LocalDate> consultarApiFestivos(int anio) {
        try {
            // A. Configuración: Simulamos ser un navegador (Mozilla) para que la API no nos bloquee.
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // B. Petición: Hacemos el GET a la URL externa pasando el año dinámicamente.
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {},
                    anio
            );

            // C. Procesamiento: Si la API responde datos, convertimos el JSON a fechas de Java (LocalDate).
            if (response.getBody() != null) {
                return response.getBody().stream()
                        .map(festivo -> LocalDate.parse((String) festivo.get("date")))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            // D. Manejo de errores: Si falla internet o la API, registramos el error y retornamos lista vacía.
            System.err.println("Error consultando API de festivos: " + e.getMessage());
        }
        return Collections.emptyList();
    }
}