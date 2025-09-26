package com.example.Gericare.Repository;

import com.example.Gericare.entity.Paciente;
import com.example.Gericare.entity.PacienteAsignado;
import com.example.Gericare.enums.EstadoAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacienteAsignadoRepository extends JpaRepository<PacienteAsignado, Long> {
    List<PacienteAsignado> findByPacienteAndEstado(Paciente paciente, EstadoAsignacion estado);
}