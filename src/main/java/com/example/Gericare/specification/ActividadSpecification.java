package com.example.Gericare.specification;

import com.example.Gericare.entity.Actividad;
import com.example.Gericare.enums.EstadoActividad;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ActividadSpecification {

    public static Specification<Actividad> findByCriteria(String nombrePaciente, String tipoActividad, EstadoActividad estado) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // AÑADIDO: Filtro principal para el borrado lógico.
            // Esto asegura que NUNCA se muestren las actividades marcadas como 'INACTIVO'.
            predicates.add(criteriaBuilder.notEqual(root.get("estadoActividad"), EstadoActividad.Inactivo));

            // Filtro por nombre de paciente (buscando en la entidad relacionada)
            if (StringUtils.hasText(nombrePaciente)) {
                // Concatena nombre y apellido del paciente para una búsqueda completa.
                var nombreCompletoPaciente = criteriaBuilder.concat(root.join("paciente", JoinType.LEFT).get("nombre"), " ");
                nombreCompletoPaciente = criteriaBuilder.concat(nombreCompletoPaciente, root.join("paciente", JoinType.LEFT).get("apellido"));
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(nombreCompletoPaciente), "%" + nombrePaciente.toLowerCase() + "%"));
            }

            // Filtro por tipo de actividad
            if (StringUtils.hasText(tipoActividad)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("tipoActividad")), "%" + tipoActividad.toLowerCase() + "%"));
            }

            // Filtro por estado
            if (estado != null) {
                predicates.add(criteriaBuilder.equal(root.get("estadoActividad"), estado));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

