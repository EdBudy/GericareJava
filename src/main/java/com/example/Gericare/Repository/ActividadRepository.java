package com.example.Gericare.Repository;

import com.example.Gericare.DTO.ActividadDTO;
import com.example.Gericare.entity.Actividad;
import com.example.Gericare.enums.EstadoActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long>, JpaSpecificationExecutor<Actividad> {

    /**
     * Busca todas las actividades pendientes de un cuidador específico.
     * Construye un DTO con los datos necesarios para la vista del cuidador.
     * @param cuidadorId El ID del cuidador.
     * @return Una lista de ActividadDTO con las actividades pendientes.
     */
    @Query("SELECT new com.example.Gericare.DTO.ActividadDTO(" +
            "a.idActividad, a.tipoActividad, a.descripcionActividad, a.fechaActividad, a.horaInicio, a.horaFin, " +
            "p.nombre, p.apellido) " +
            "FROM Actividad a JOIN a.paciente p JOIN PacienteAsignado pa ON pa.paciente.idPaciente = p.idPaciente " +
            "WHERE pa.cuidador.idUsuario = :cuidadorId AND a.estadoActividad = com.example.Gericare.enums.EstadoActividad.Pendiente")
    List<ActividadDTO> findActividadesPendientesByCuidador(@Param("cuidadorId") Long cuidadorId);
}

