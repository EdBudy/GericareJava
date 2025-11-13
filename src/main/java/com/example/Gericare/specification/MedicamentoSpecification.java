package com.example.Gericare.specification;

import com.example.Gericare.Entity.Medicamento;
import com.example.Gericare.Enums.EstadoUsuario;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MedicamentoSpecification {

    public static Specification<Medicamento> findByCriteria(String nombre, String descripcion) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // solo mostrar medicamentos activos
            predicates.add(criteriaBuilder.equal(root.get("estado"), EstadoUsuario.Activo));

            // Filtro por nombre (ignora mayúsculas/minúsculas)
            if (StringUtils.hasText(nombre)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nombreMedicamento")),
                        "%" + nombre.toLowerCase() + "%"
                ));
            }

            // Filtro por descripción (ignora mayúsculas/minúsculas)
            if (StringUtils.hasText(descripcion)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("descripcionMedicamento")),
                        "%" + descripcion.toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}