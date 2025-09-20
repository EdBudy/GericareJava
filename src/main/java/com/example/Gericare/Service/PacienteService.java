package com.example.Gericare.Service;

import com.example.Gericare.DTO.PacienteDTO;

import java.util.List;

public interface PacienteService {

    List<PacienteDTO> listarPacientes();

    PacienteDTO obtenerPacientePorId(Long idPaciente);

    PacienteDTO crearPaciente(PacienteDTO PacienteDTO);

    PacienteDTO actualizarPaciente(Long idPaciente, PacienteDTO PacienteDTO);

    void eliminarPaciente(Long idPaciente);

}
