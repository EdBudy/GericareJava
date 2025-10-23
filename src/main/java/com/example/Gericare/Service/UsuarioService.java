package com.example.Gericare.Service;

import com.example.Gericare.DTO.PacienteAsignadoDTO;
import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Entity.Cuidador;
import com.example.Gericare.Entity.Familiar;
import com.example.Gericare.Enums.RolNombre;
import java.io.IOException;
import java.io.OutputStream;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    UsuarioDTO crearCuidador(Cuidador cuidador);

    UsuarioDTO crearFamiliar(Familiar familiar);

    List<UsuarioDTO> listarTodosLosUsuarios();

    Optional<UsuarioDTO> obtenerUsuarioPorId(Long id);

    void eliminarUsuario(Long id);

    Optional<UsuarioDTO> actualizarUsuario(Long id, UsuarioDTO usuarioDTO);

    Optional<UsuarioDTO> findByEmail(String email);

    List<UsuarioDTO> findByRol(RolNombre rolNombre);

    // Métodos para el dashboard y filtros
    List<UsuarioDTO> findUsuariosByCriteria(String nombre, String documento, RolNombre rol, String emailToExclude);

    List<PacienteAsignadoDTO> findPacientesByCuidadorEmail(String email);

    List<PacienteAsignadoDTO> findPacientesByFamiliarEmail(String email);

    // Métodos para exportar
    void exportarUsuariosAExcel(OutputStream outputStream, String nombre, String documento, RolNombre rol) throws IOException;
    void exportarUsuariosAPDF(OutputStream outputStream, String nombre, String documento, RolNombre rol) throws IOException;

    // Metodos correo/token
    void createPasswordResetTokenForUser(String email);
    String validatePasswordResetToken(String token);
    void changeUserPassword(String token, String newPassword);
}
