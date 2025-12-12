package com.example.Gericare.Service;

import com.example.Gericare.DTO.PacienteAsignadoDTO;
import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Entity.Cuidador;
import com.example.Gericare.Entity.Familiar;
import com.example.Gericare.Enums.RolNombre;
import java.io.IOException;
import java.io.OutputStream;

import com.example.Gericare.Enums.RolNombre; // Necesario para el nuevo método

import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    // crea un cuidador en el sistema
    UsuarioDTO crearCuidador(Cuidador cuidador);

    // crea un familiar en el sistema
    UsuarioDTO crearFamiliar(Familiar familiar);

    // retorna todos los usuarios registrados
    List<UsuarioDTO> listarTodosLosUsuarios();

    // busca un usuario por su id
    Optional<UsuarioDTO> obtenerUsuarioPorId(Long id);

    // elimina un usuario por id
    void eliminarUsuario(Long id);

    // actualiza la información de un usuario
    Optional<UsuarioDTO> actualizarUsuario(Long id, UsuarioDTO usuarioDTO);

    // busca un usuario por correo
    Optional<UsuarioDTO> findByEmail(String email);

    // obtiene usuarios según su rol
    List<UsuarioDTO> findByRol(RolNombre rolNombre);

    // busca usuarios según filtros personalizados
    List<UsuarioDTO> findUsuariosByCriteria(String nombre, String documento, RolNombre rol);

    // retorna pacientes asignados a un cuidador por su correo
    List<PacienteAsignadoDTO> findPacientesByCuidadorEmail(String email);

    // retorna pacientes asignados a un familiar por su correo
    List<PacienteAsignadoDTO> findPacientesByFamiliarEmail(String email);

    // exporta usuarios a excel
    void exportarUsuariosAExcel(OutputStream outputStream, String nombre, String documento, RolNombre rol) throws IOException;

    // exporta usuarios a pdf
    void exportarUsuariosAPDF(OutputStream outputStream, String nombre, String documento, RolNombre rol) throws IOException;

    // crea un token de reseteo de contraseña
    void createPasswordResetTokenForUser(String email);

    // valida un token de reseteo
    String validatePasswordResetToken(String token);

    // cambia la contraseña usando un token de validación
    void changeUserPassword(String token, String newPassword);

    // envia un correo masivo según el rol
    void sendCustomBulkEmailToRole(RolNombre role, String subject, String body);
}

