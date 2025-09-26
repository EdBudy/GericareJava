package com.example.gericare.Repository;

import com.example.gericare.entity.Paciente;
import com.example.gericare.entity.PacienteAsignado;
import com.example.gericare.enums.EstadoAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacienteAsignadoRepository extends JpaRepository<PacienteAsignado, Long> {
    List<PacienteAsignado> findByPacienteAndEstado(Paciente paciente, EstadoAsignacion estado);
}