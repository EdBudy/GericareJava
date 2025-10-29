package com.example.Gericare.Service;

import com.example.Gericare.DTO.HistoriaClinicaDTO;
import com.example.Gericare.Entity.Paciente;
import com.example.Gericare.Entity.Administrador;

import java.util.Optional;

public interface HistoriaClinicaService {

    // Metodo para crear automáticamente la HC básica al crear paciente
    void crearHistoriaClinicaInicial(Paciente paciente, Administrador admin);

    Optional<HistoriaClinicaDTO> obtenerHistoriaClinicaPorId(Long id);

    Optional<HistoriaClinicaDTO> obtenerHistoriaClinicaPorPacienteId(Long pacienteId);

    HistoriaClinicaDTO actualizarHistoriaClinica(Long id, HistoriaClinicaDTO historiaClinicaDTO, Long adminId);
}