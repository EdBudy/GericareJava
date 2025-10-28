package com.example.Gericare.Service;

import com.example.Gericare.DTO.TratamientoDTO;
import java.util.List;
import java.util.Optional;

public interface TratamientoService {

    TratamientoDTO crearTratamiento(TratamientoDTO tratamientoDTO, Long adminId);

    Optional<TratamientoDTO> obtenerTratamientoPorId(Long id);

    List<TratamientoDTO> listarTodosTratamientosActivos(); // Admin

    List<TratamientoDTO> listarTratamientosActivosPorCuidador(Long cuidadorId); // Cuidador

    List<TratamientoDTO> listarTratamientosActivosPorPaciente(Long pacienteId); // Familiar

    // Actualización diferenciada por rol
    Optional<TratamientoDTO> actualizarTratamientoAdmin(Long id, TratamientoDTO tratamientoDTO);
    Optional<TratamientoDTO> actualizarObservacionesCuidador(Long id, Long cuidadorId, String observaciones);

    // Completar tratamiento (Cuidador)
    Optional<TratamientoDTO> completarTratamiento(Long id, Long cuidadorId);

    // Borrado lógico (Admin)
    void eliminarTratamientoLogico(Long id);

    void desactivarTratamientosPorPaciente(Long pacienteId);
}