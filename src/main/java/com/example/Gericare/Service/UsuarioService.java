package com.example.Gericare.Service;

import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.entity.Cuidador;
import com.example.Gericare.entity.Familiar;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    UsuarioDTO crearCuidador(Cuidador cuidador);

    UsuarioDTO crearFamiliar(Familiar familiar);

    List<UsuarioDTO> listarTodosLosUsuarios();

    Optional<UsuarioDTO> obtenerUsuarioPorId(Long id);

    void eliminarUsuario(Long id);

    Optional<UsuarioDTO> actualizarUsuario(Long id, UsuarioDTO usuarioDTO);
}