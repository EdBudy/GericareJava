package com.example.Gericare.Service;

import com.example.Gericare.DTO.UsuarioDTO;

import java.util.List;

public interface UsuarioService {

    List<UsuarioDTO> listarUsuarios();

    UsuarioDTO obtenerUsuarioPorId(Long idUsuario);

    UsuarioDTO crearUsuario(UsuarioDTO usuarioDTO);

    UsuarioDTO actualizarUsuario(Long idUsuario, UsuarioDTO usuarioDTO);

    void eliminarUsuario(Long idUsuario);
}
