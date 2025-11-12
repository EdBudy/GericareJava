package com.example.Gericare.Repository;

import com.example.Gericare.Entity.Tratamiento;
import com.example.Gericare.Enums.EstadoActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TratamientoRepository extends JpaRepository<Tratamiento, Long>, JpaSpecificationExecutor<Tratamiento> {

    // Desactivación cascada Paciente
    List<Tratamiento> findByPacienteIdPaciente(Long pacienteId);

    // Vista Cuidador (pacientes asignados actualmente)
    // Necesario join con PacienteAsignado pa filtrar por cuidadorId y estado ACTIVO de la asignación
    @Query("SELECT t FROM Tratamiento t JOIN PacienteAsignado pa ON t.paciente.idPaciente = pa.paciente.idPaciente " +
            "WHERE pa.cuidador.idUsuario = :cuidadorId AND pa.estado = com.example.Gericare.Enums.EstadoAsignacion.Activo " +
            "AND t.estadoTratamiento <> com.example.Gericare.Enums.EstadoActividad.Inactivo")
    List<Tratamiento> findTratamientosActivosByCuidadorAsignado(@Param("cuidadorId") Long cuidadorId);

    List<Tratamiento> findByEstadoTratamiento(EstadoActividad estado);

}