package com.example.Gericare.specification;

import com.example.Gericare.entity.Paciente;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PacienteSpecification {

    public static Specification<Paciente> findByCriteria(String nombre, String documento) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Búsqueda por nombre completo (concatenando nombre y apellido)
            if (StringUtils.hasText(nombre)) {
                Expression<String> nombreCompleto = criteriaBuilder.concat(root.get("nombre"), " ");
                nombreCompleto = criteriaBuilder.concat(nombreCompleto, root.get("apellido"));
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(nombreCompleto), "%" + nombre.toLowerCase() + "%"));
            }

            // Búsqueda por documento
            if (StringUtils.hasText(documento)) {
                predicates.add(criteriaBuilder.like(root.get("documentoIdentificacion"), "%" + documento + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}