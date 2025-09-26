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

    public static Specification<Usuario> findByCriteria(String nombre, String documento, RolNombre rol) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Si se proporciona un nombre, se añade un filtro 'like' para buscar coincidencias parciales.
            if (StringUtils.hasText(nombre)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));
            }

            // Si se proporciona un documento, se añade un filtro 'like'.
            if (StringUtils.hasText(documento)) {
                predicates.add(criteriaBuilder.like(root.get("documentoIdentificacion"), "%" + documento + "%"));
            }

            // Si se proporciona un rol, se filtra por el nombre del rol.
            if (rol != null) {
                predicates.add(criteriaBuilder.equal(root.get("rol").get("rolNombre"), rol));
            }

            // Se combinan todos los predicados con un 'AND'.
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}