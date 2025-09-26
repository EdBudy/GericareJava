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

    // Encuentra todas las asignaciones activas de un cuidador a través de su correo electrónico
    List<PacienteAsignado> findByCuidador_CorreoElectronicoAndEstado(String correoElectronico, EstadoAsignacion estado);

    // Encuentra todas las asignaciones activas de un familiar a través de su correo electrónico
    Optional<PacienteAsignado> findByFamiliar_CorreoElectronicoAndEstado(String correoElectronico, EstadoAsignacion estado);
}
