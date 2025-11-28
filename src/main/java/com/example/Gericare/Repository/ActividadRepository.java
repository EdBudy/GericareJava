package com.example.Gericare.Repository;

import com.example.Gericare.DTO.ActividadDTO;
import com.example.Gericare.Entity.Actividad;
import com.example.Gericare.Entity.Cuidador;
import com.example.Gericare.Enums.EstadoActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long>, JpaSpecificationExecutor<Actividad> {

    List<Actividad> findByPacienteIdPaciente(Long idPaciente);

    @Query("SELECT new com.example.Gericare.DTO.ActividadDTO(" +
            "a.idActividad, a.tipoActividad, a.descripcionActividad, a.fechaActividad, a.horaInicio, a.horaFin, " +
            "p.nombre, p.apellido, a.estadoActividad) " +
            "FROM Actividad a JOIN a.paciente p JOIN PacienteAsignado pa ON pa.paciente.idPaciente = p.idPaciente " +
            "WHERE pa.cuidador.idUsuario = :cuidadorId AND a.estadoActividad IN (com.example.Gericare.Enums.EstadoActividad.Pendiente, com.example.Gericare.Enums.EstadoActividad.Completada)")
    List<ActividadDTO> findActividadesByCuidador(@Param("cuidadorId") Long cuidadorId);

    // Métodos estadísticas

    // Contar total actividades de pacientes asignados
    @Query("SELECT COUNT(a) FROM Actividad a " +
            "JOIN a.paciente p " +
            "JOIN PacienteAsignado pa ON pa.paciente = p " +
            "WHERE pa.cuidador = :cuidador AND pa.estado = com.example.Gericare.Enums.EstadoAsignacion.Activo")
    Long countTotalActividadesAsignadas(@Param("cuidador") Cuidador cuidador);

    // Contar actividades completadas
    @Query("SELECT COUNT(a) FROM Actividad a " +
            "JOIN a.paciente p " +
            "JOIN PacienteAsignado pa ON pa.paciente = p " +
            "WHERE pa.cuidador = :cuidador " +
            "AND pa.estado = com.example.Gericare.Enums.EstadoAsignacion.Activo " +
            "AND a.estadoActividad = :estado")
    Long countActividadesByCuidadorAndEstado(@Param("cuidador") Cuidador cuidador, @Param("estado") EstadoActividad estado);
}