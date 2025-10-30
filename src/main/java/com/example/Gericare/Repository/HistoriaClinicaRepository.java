package com.example.Gericare.Repository;

import com.example.Gericare.Entity.HistoriaClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Long> {
    Optional<HistoriaClinica> findByPacienteIdPaciente(Long pacienteId);

    // Para cargar datos relacionados EAGER al buscar por ID (evita LazyInitializationException)
    @Query("SELECT hc FROM HistoriaClinica hc " +
            "LEFT JOIN FETCH hc.cirugias c " +
            "LEFT JOIN FETCH hc.medicamentos hm LEFT JOIN FETCH hm.medicamento m " +
            "LEFT JOIN FETCH hc.enfermedades he " +
            "WHERE hc.idHistoriaClinica = :id")
    Optional<HistoriaClinica> findByIdWithDetails(Long id);

    @Query("SELECT hc FROM HistoriaClinica hc " +
            "LEFT JOIN FETCH hc.cirugias c " +
            "LEFT JOIN FETCH hc.medicamentos hm LEFT JOIN FETCH hm.medicamento m " +
            "LEFT JOIN FETCH hc.enfermedades he " +
            "WHERE hc.paciente.idPaciente = :pacienteId")
    Optional<HistoriaClinica> findByPacienteIdWithDetails(Long pacienteId);
}