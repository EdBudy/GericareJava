package com.example.Gericare.Repository;

import com.example.Gericare.Entity.Solicitud;
import com.example.Gericare.Enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long>, JpaSpecificationExecutor<Solicitud> {

    // Vista del Familiar
    List<Solicitud> findByFamiliarIdUsuario(Long familiarId);

    // Desactivación en cascada al desactivar Paciente
    List<Solicitud> findByPacienteIdPaciente(Long pacienteId);

    // Pbtener solicitudes activas
    List<Solicitud> findByEstadoSolicitudNot(EstadoSolicitud estado);
    List<Solicitud> findByFamiliarIdUsuarioAndEstadoSolicitudNot(Long familiarId, EstadoSolicitud estado);
    List<Solicitud> findByPacienteIdPacienteAndEstadoSolicitudNot(Long pacienteId, EstadoSolicitud estado);
}