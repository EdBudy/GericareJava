package com.example.Gericare.Service;

import com.example.Gericare.DTO.ActividadDTO;
import com.example.Gericare.enums.EstadoActividad;

import java.util.List;
import java.util.Optional;

public interface ActividadService {

    // Corregido: El nombre del método ahora coincide con la implementación.
    List<ActividadDTO> listarActividades(String nombrePaciente, String tipoActividad, EstadoActividad estado);

    ActividadDTO crearActividad(ActividadDTO actividadDTO);

    Optional<ActividadDTO> obtenerActividadPorId(Long id);

    // Corregido: El tipo de retorno ahora es Optional<ActividadDTO> para coincidir con la implementación.
    Optional<ActividadDTO> actualizarActividad(Long id, ActividadDTO actividadDTO);

    void eliminarActividad(Long id);

    List<ActividadDTO> listarActividadesPorCuidador(Long cuidadorId);

    void marcarComoCompletada(Long actividadId, Long cuidadorId);
}

