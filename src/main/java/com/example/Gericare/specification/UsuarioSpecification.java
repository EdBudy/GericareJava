package com.example.Gericare.specification; // Paquete corregido

import com.example.Gericare.entity.Usuario;
import com.example.Gericare.enums.RolNombre;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

// Clase para construir consultas dinámicas para la entidad Usuario
public class UsuarioSpecification {

    public static Specification<Usuario> findByCriteria(String nombre, String documento, RolNombre rol, String emailToExclude) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ... (los otros filtros de nombre, documento y rol se mantienen igual)
            if (StringUtils.hasText(nombre)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(documento)) {
                predicates.add(criteriaBuilder.like(root.get("documentoIdentificacion"), "%" + documento + "%"));
            }
            if (rol != null) {
                predicates.add(criteriaBuilder.equal(root.get("rol").get("rolNombre"), rol));
            }

            // ¡NUEVA LÓGICA! Añade un predicado para excluir el correo del admin actual
            if (StringUtils.hasText(emailToExclude)) {
                predicates.add(criteriaBuilder.notEqual(root.get("correoElectronico"), emailToExclude));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}