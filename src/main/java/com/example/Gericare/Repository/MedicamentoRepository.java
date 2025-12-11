package com.example.Gericare.Repository;

import com.example.Gericare.Entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Indica a Spring que esto es un componente de acceso a datos
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long>, JpaSpecificationExecutor<Medicamento> {

    // Busca un medicamento por nombre ignorando mayúsculas y minúsculas
    Optional<Medicamento> findByNombreMedicamentoIgnoreCase(String nombreMedicamento);
}