package com.example.Gericare.Repository;

import com.example.Gericare.DTO.EstadisticaCuidadorDTO;
import com.example.Gericare.Entity.Paciente;
import com.example.Gericare.Entity.PacienteAsignado;
import com.example.Gericare.Enums.EstadoAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteAsignadoRepository extends JpaRepository<PacienteAsignado, Long> {
    // encuentra asignaciones por paciente y estado
    List<PacienteAsignado> findByPacienteAndEstado(Paciente paciente, EstadoAsignacion estado);

    // encuentra asignaciones por correo electrónico del cuidador y estado
    List<PacienteAsignado> findByCuidador_CorreoElectronicoAndEstado(String correoElectronico, EstadoAsignacion estado);

    // encuentra asignaciones por correo electrónico del familiar y estado
    List<PacienteAsignado> findByFamiliar_CorreoElectronicoAndEstado(String correoElectronico, EstadoAsignacion estado);

    // encuentra asignaciones por id de paciente y estado
    List<PacienteAsignado> findByPacienteIdPacienteAndEstado(Long idPaciente, EstadoAsignacion estado);

    // valida si existe una asignación activa para un cuidador y paciente específicos
    Optional<PacienteAsignado> findByCuidador_idUsuarioAndPaciente_idPacienteAndEstado(Long cuidadorId, Long pacienteId, EstadoAsignacion estado);

    // encuentra todas las asignaciones de un paciente (sin filtrar por estado)
    List<PacienteAsignado> findByPacienteIdPaciente(Long idPaciente);

    // encuentra asignaciones por id de cuidador y estado
    List<PacienteAsignado> findByCuidador_idUsuarioAndEstado(Long cuidadorId, EstadoAsignacion estado);

    // encuentra asignaciones por id de familiar
    List<PacienteAsignado> findByFamiliar_idUsuario(Long familiarId);

    // consulta personalizada para obtener estadísticas de cuidadores
    // cuenta cuántos pacientes activos tiene cada cuidador y devuelve un dto
    @Query("SELECT new com.example.Gericare.DTO.EstadisticaCuidadorDTO(c.nombre, c.apellido, COUNT(pa)) " +
            "FROM PacienteAsignado pa " +
            "JOIN pa.cuidador c " +
            "WHERE pa.estado = com.example.Gericare.Enums.EstadoAsignacion.Activo " +
            "GROUP BY c.idUsuario, c.nombre, c.apellido")
    List<EstadisticaCuidadorDTO> obtenerPacientesPorCuidador();
}