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

            // Búsqueda por nombre o apellido
            if (nombre != null && !nombre.isEmpty()) {
                String likePattern = "%" + nombre.toLowerCase() + "%";

                // Crea predicado para buscar en 'nombre'
                Predicate nombreLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nombre")),
                        likePattern
                );

                // Crea predicado para buscar en 'apellido'
                Predicate apellidoLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("apellido")),
                        likePattern
                );

                // Combina ambos con un OR
                predicates.add(criteriaBuilder.or(nombreLike, apellidoLike));
            }

            if (documento != null && !documento.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("documentoIdentificacion"), "%" + documento + "%"));
            }
            if (rol != null) {
                predicates.add(criteriaBuilder.equal(root.get("rol").get("rolNombre"), rol));
            }

            // Excluir administradores de la lista
            predicates.add(criteriaBuilder.notEqual(root.get("rol").get("rolNombre"), RolNombre.Administrador));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}