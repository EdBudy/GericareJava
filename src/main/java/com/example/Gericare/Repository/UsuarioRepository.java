package com.example.Gericare.Repository;

import com.example.Gericare.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
// JpaSpecificationExecutor permite el uso de Specifications para queries dinámicas.
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    // Spring Data JPA creará la consulta automáticamente por el nombre del metodo.
    // Esto se traduce a: "SELECT * FROM tb_usuario WHERE correo_electronico = ?"
    Optional<Usuario> findByCorreoElectronico(String correoElectronico);
}
