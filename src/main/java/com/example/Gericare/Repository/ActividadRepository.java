package com.example.Gericare.Repository;

import com.example.Gericare.DTO.ActividadDTO;
import com.example.Gericare.DTO.EstadisticaActividadDTO;
import com.example.Gericare.Entity.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long>, JpaSpecificationExecutor<Actividad> {

    @Query("SELECT new com.example.Gericare.DTO.ActividadDTO(" +
            "a.idActividad, a.tipoActividad, a.descripcionActividad, a.fechaActividad, a.horaInicio, a.horaFin, " +
            "p.nombre, p.apellido, a.estadoActividad) " +
            "FROM Actividad a JOIN a.paciente p JOIN PacienteAsignado pa ON pa.paciente.idPaciente = p.idPaciente " +
            "WHERE pa.cuidador.idUsuario = :cuidadorId AND a.estadoActividad IN (com.example.Gericare.Enums.EstadoActividad.Pendiente, com.example.Gericare.Enums.EstadoActividad.Completada)")
    List<ActividadDTO> findActividadesByCuidador(@Param("cuidadorId") Long cuidadorId);

    List<Actividad> findByPacienteIdPaciente(Long pacienteId);

    // NUEVA CONSULTA: Cuenta actividades completadas agrupadas por cuidador
    @Query("SELECT new com.example.Gericare.DTO.EstadisticaActividadDTO(u.nombre, u.apellido, COUNT(a)) " +
            "FROM Actividad a " +
            "JOIN a.paciente p " +
            "JOIN PacienteAsignado pa ON pa.paciente.idPaciente = p.idPaciente " +
            "JOIN pa.cuidador u " +
            "WHERE a.estadoActividad = com.example.Gericare.Enums.EstadoActividad.Completada " +
            "GROUP BY u.idUsuario, u.nombre, u.apellido")
    List<EstadisticaActividadDTO> countActividadesCompletadasPorCuidador();
}