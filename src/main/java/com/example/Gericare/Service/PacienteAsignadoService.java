package com.example.Gericare.Service;

import com.example.Gericare.DTO.PacienteAsignadoDTO;
import java.util.List;
import java.util.Optional;

public interface PacienteAsignadoService {

    PacienteAsignadoDTO crearAsignacion(Long idPaciente, Long idCuidador, Long idFamiliar, Long idAdmin);
    // Obtener todas las asignaciones (solo las activas debido a @Where).
    List<PacienteAsignadoDTO> listarTodasLasAsignaciones();
    Optional<PacienteAsignadoDTO> obtenerAsignacionPorId(Long idAsignacion);
    // Desactivar una asignación (borrado lógico).
    void eliminarAsignacion(Long idAsignacion);
}