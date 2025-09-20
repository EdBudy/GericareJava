package com.example.Gericare.Repository;

import com.example.Gericare.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Spring Data JPA creará la consulta automáticamente por el nombre del método.
    // Esto se traduce a: "SELECT * FROM tb_usuario WHERE correo_electronico = ?"
    Optional<Usuario> findByCorreoElectronico(String correo);
}
