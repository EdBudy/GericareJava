package com.example.Gericare.specification;

import com.example.Gericare.entity.Usuario;
import com.example.Gericare.enums.RolNombre;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UsuarioSpecification {

    public static Specification<Usuario> findByCriteria(String nombre, String documento, RolNombre rol, String emailToExclude) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // busqueda por nombre completo
            if (StringUtils.hasText(nombre)) {
                // Crea una expresión que concatena el nombre, un espacio y el apellido.
                Expression<String> nombreCompleto = criteriaBuilder.concat(root.get("nombre"), " ");
                nombreCompleto = criteriaBuilder.concat(nombreCompleto, root.get("apellido"));

                // Añade un predicado 'like' que busca en el nombre completo (ignorando mayúsculas/minúsculas).
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(nombreCompleto), "%" + nombre.toLowerCase() + "%"));
            }

            if (StringUtils.hasText(documento)) {
                predicates.add(criteriaBuilder.like(root.get("documentoIdentificacion"), "%" + documento + "%"));
            }

            if (rol != null) {
                predicates.add(criteriaBuilder.equal(root.get("rol").get("rolNombre"), rol));
            }

            if (StringUtils.hasText(emailToExclude)) {
                predicates.add(criteriaBuilder.notEqual(root.get("correoElectronico"), emailToExclude));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}