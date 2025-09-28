package com.example.Gericare.Service;

import com.example.Gericare.DTO.PacienteAsignadoDTO;
import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.entity.Cuidador;
import com.example.Gericare.entity.Familiar;
import com.example.Gericare.enums.RolNombre;
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

    // Nuevos métodos para el dashboard y filtros
    List<UsuarioDTO> findUsuariosByCriteria(String nombre, String documento, RolNombre rol, String emailToExclude);

    List<PacienteAsignadoDTO> findPacientesByCuidadorEmail(String email);

    Optional<PacienteAsignadoDTO> findPacientesByFamiliarEmail(String email);

    // Nuevos métodos para exportar
    void exportarUsuariosAExcel(OutputStream outputStream) throws IOException;
    void exportarUsuariosAPDF(OutputStream outputStream) throws IOException;
}
