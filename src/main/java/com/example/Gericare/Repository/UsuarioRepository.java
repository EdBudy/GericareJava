package com.example.Gericare.Repository;

import com.example.Gericare.Entity.Usuario;
import com.example.Gericare.Enums.RolNombre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
// JpaSpecificationExecutor permite el uso de Specifications para queries dinámicas
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    // Spring Data JPA crea la consulta automáticamente por el nombre del metodo
    Optional<Usuario> findByCorreoElectronico(String correoElectronico);
    List<Usuario> findByRol_RolNombre(RolNombre rolNombre);
   Optional<Usuario> findByResetPasswordToken(String token);

    // Busca usuarios ignorando el filtro de "Activo"
    @Query(value = "SELECT * FROM usuarios WHERE documento_identificacion = :doc OR correo_electronico = :email LIMIT 1", nativeQuery = true)
    Optional<Usuario> findByDocumentoOrEmailNative(@Param("doc") String doc, @Param("email") String email);
}
