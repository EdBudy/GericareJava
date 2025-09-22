package com.example.Gericare.Service;

import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.entity.Usuario;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

// La interfaz extiende UserDetailsService para integrarse con Spring Security
public interface UsuarioService extends UserDetailsService {

    // Define un metodo para guardar un nuevo usuario a partir de un DTO
    Usuario guardar(UsuarioDTO usuarioDTO);

    List<UsuarioDTO> listarUsuarios();

    UsuarioDTO obtenerUsuarioPorId(Long idUsuario);

    UsuarioDTO crearUsuario(UsuarioDTO usuarioDTO);

    UsuarioDTO actualizarUsuario(Long idUsuario, UsuarioDTO usuarioDTO);

    void eliminarUsuario(Long idUsuario);
}