package com.example.Gericare.specification;

import com.example.Gericare.Entity.Usuario;
import com.example.Gericare.Enums.EstadoUsuario;
import com.example.Gericare.Enums.RolNombre;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UsuarioSpecification {

    public static Specification<Usuario> findByCriteria(String nombre, String documento, RolNombre rol) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("estado"), EstadoUsuario.Activo));

            if (nombre != null && !nombre.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));
            }
            if (documento != null && !documento.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("documentoIdentificacion"), "%" + documento + "%"));
            }
            if (rol != null) {
                predicates.add(criteriaBuilder.equal(root.get("rol").get("rolNombre"), rol));
            }

            // Excluir siempre a los administradores de la lista
            predicates.add(criteriaBuilder.notEqual(root.get("rol").get("rolNombre"), RolNombre.Administrador));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}