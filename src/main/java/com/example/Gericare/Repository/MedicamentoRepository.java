package com.example.Gericare.Repository;

import com.example.Gericare.Entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    // Busca un medicamento por nombre ignorando mayúsculas y minúsculas
    Optional<Medicamento> findByNombreMedicamentoIgnoreCase(String nombreMedicamento);
}