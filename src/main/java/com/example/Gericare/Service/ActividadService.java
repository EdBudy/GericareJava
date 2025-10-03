package com.example.Gericare.Service;

import com.example.Gericare.DTO.ActividadDTO;
import com.example.Gericare.enums.EstadoActividad;

import java.util.List;
import java.util.Optional;

public interface ActividadService {

    List<ActividadDTO> listarActividades(String nombrePaciente, String tipoActividad, EstadoActividad estado);

    ActividadDTO crearActividad(ActividadDTO actividadDTO);

    Optional<ActividadDTO> obtenerActividadPorId(Long id);

    Optional<ActividadDTO> actualizarActividad(Long id, ActividadDTO actividadDTO);

    void eliminarActividad(Long id);

    List<ActividadDTO> listarActividadesPorCuidador(Long cuidadorId);

    void marcarComoCompletada(Long actividadId, Long cuidadorId);
}

