package com.example.Gericare.Repository;

import com.example.Gericare.entity.PacienteAsignado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteAsignadoRepository extends JpaRepository<PacienteAsignado, Long> {
}