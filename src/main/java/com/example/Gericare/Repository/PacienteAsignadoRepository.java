package com.example.Gericare.Repository;

import com.example.Gericare.entity.Paciente;
import com.example.Gericare.entity.PacienteAsignado;
import com.example.Gericare.enums.EstadoAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteAsignadoRepository extends JpaRepository<PacienteAsignado, Long> {
    List<PacienteAsignado> findByPacienteAndEstado(Paciente paciente, EstadoAsignacion estado);

    List<PacienteAsignado> findByCuidador_CorreoElectronicoAndEstado(String correoElectronico, EstadoAsignacion estado);

    Optional<PacienteAsignado> findByFamiliar_CorreoElectronicoAndEstado(String correoElectronico, EstadoAsignacion estado);

    List<PacienteAsignado> findByPacienteIdPacienteAndEstado(Long idPaciente, EstadoAsignacion estado);

    /**
     * Valida si existe una asignación activa para una combinación específica de cuidador y paciente.
     * Esencial para la validación de seguridad.
     */
    Optional<PacienteAsignado> findByCuidador_idUsuarioAndPaciente_idPacienteAndEstado(Long cuidadorId, Long pacienteId, EstadoAsignacion estado);
}
