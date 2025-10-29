package com.example.Gericare.Service;

import com.example.Gericare.DTO.EnfermedadDTO;
import java.util.List;
import java.util.Optional;

public interface EnfermedadService {

    // Lista todas las enfermedades activas.
    List<EnfermedadDTO> listarEnfermedadesActivas();

    // Guarda una nueva enfermedad o actualiza una existente.
    EnfermedadDTO guardarEnfermedad(EnfermedadDTO dto);

    // Elimina una enfermedad (lógica de desactivación).
    void eliminarEnfermedad(Long id);

    // Obtiene una enfermedad por su ID.
    Optional<EnfermedadDTO> obtenerEnfermedadPorId(Long id);
}