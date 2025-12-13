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

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long>, JpaSpecificationExecutor<Actividad> {

    // encuentra actividades por id de paciente
    List<Actividad> findByPacienteIdPaciente(Long idPaciente);

    // consulta personalizada para obtener actividades de un cuidador específico
    @Query("SELECT new com.example.Gericare.DTO.ActividadDTO(" +
            "a.idActividad, a.tipoActividad, a.descripcionActividad, a.fechaActividad, a.horaInicio, a.horaFin, " +
            "p.nombre, p.apellido, a.estadoActividad) " +
            "FROM Actividad a JOIN a.paciente p JOIN PacienteAsignado pa ON pa.paciente.idPaciente = p.idPaciente " +
            "WHERE pa.cuidador.idUsuario = :cuidadorId AND a.estadoActividad IN (com.example.Gericare.Enums.EstadoActividad.Pendiente, com.example.Gericare.Enums.EstadoActividad.Completada)")
    List<ActividadDTO> findActividadesByCuidador(@Param("cuidadorId") Long cuidadorId);

    // métodos para estadísticas:

    // cuenta el total de actividades asignadas a un cuidador (sin filtro fecha)
    // (para el pdf que muestra totales)
    @Query("SELECT COUNT(a) FROM Actividad a JOIN a.paciente p JOIN PacienteAsignado pa ON pa.paciente = p " +
            "WHERE pa.cuidador = :cuidador " +
            "AND pa.estado = com.example.Gericare.Enums.EstadoAsignacion.Activo " +
            "AND a.estadoActividad <> com.example.Gericare.Enums.EstadoActividad.Inactivo")
    Long countTotalActividadesAsignadas(@Param("cuidador") Cuidador cuidador);

    // cuenta actividades de un cuidador por estado (sin filtro fecha)
    // (para pdf que muestra totales)
    @Query("SELECT COUNT(a) FROM Actividad a JOIN a.paciente p JOIN PacienteAsignado pa ON pa.paciente = p " +
            "WHERE pa.cuidador = :cuidador AND pa.estado = com.example.Gericare.Enums.EstadoAsignacion.Activo AND a.estadoActividad = :estado")
    Long countActividadesByCuidadorAndEstado(@Param("cuidador") Cuidador cuidador, @Param("estado") EstadoActividad estado);

    // cuenta actividades asignadas a un cuidador en una fecha específica
    // (para el gráfico web que muestra datos de hoy)
    @Query("SELECT COUNT(a) FROM Actividad a " +
            "JOIN a.paciente p " +
            "JOIN PacienteAsignado pa ON pa.paciente = p " +
            "WHERE pa.cuidador = :cuidador " +
            "AND a.fechaActividad = :fecha " +
            "AND pa.estado = com.example.Gericare.Enums.EstadoAsignacion.Activo " +
            "AND a.estadoActividad <> com.example.Gericare.Enums.EstadoActividad.Inactivo")
    Long countActividadesAsignadasPorFecha(@Param("cuidador") Cuidador cuidador, @Param("fecha") LocalDate fecha);

    // cuenta actividades completadas por un cuidador en una fecha específica
    // (para el gráfico web que muestra datos del hoy)
    @Query("SELECT COUNT(a) FROM Actividad a " +
            "JOIN a.paciente p " +
            "JOIN PacienteAsignado pa ON pa.paciente = p " +
            "WHERE pa.cuidador = :cuidador " +
            "AND a.estadoActividad = :estado " +
            "AND a.fechaActividad = :fecha " +
            "AND pa.estado = com.example.Gericare.Enums.EstadoAsignacion.Activo")
    Long countActividadesCompletadasPorFecha(@Param("cuidador") Cuidador cuidador, @Param("estado") EstadoActividad estado, @Param("fecha") LocalDate fecha);

    // encuentra actividades por estado y con fecha anterior a una fecha dada
    List<Actividad> findByEstadoActividadAndFechaActividadBefore(EstadoActividad estado, LocalDate fecha);
}