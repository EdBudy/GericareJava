package com.example.Gericare.Repository;

import com.example.Gericare.Entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long>, JpaSpecificationExecutor<Paciente> {
    Optional<Paciente> findByDocumentoIdentificacion(String documento);
}