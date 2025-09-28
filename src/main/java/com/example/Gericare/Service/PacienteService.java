package com.example.Gericare.Service;

import com.example.Gericare.DTO.PacienteDTO;
import java.util.List;
import java.util.Optional;

public interface PacienteService {

    PacienteDTO crearPaciente(PacienteDTO pacienteDTO);

    List<PacienteDTO> listarTodosLosPacientes();

    Optional<PacienteDTO> obtenerPacientePorId(Long id);

    Optional<PacienteDTO> actualizarPaciente(Long id, PacienteDTO pacienteDTO);

    PacienteDTO crearPacienteYAsignar(PacienteDTO pacienteDTO, Long cuidadorId, Long familiarId, Long adminId);

    void eliminarPaciente(Long id); // Borrador lógico
}