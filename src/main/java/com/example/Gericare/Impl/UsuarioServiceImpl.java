package com.example.Gericare.Impl;

import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Repository.RolRepository;
import com.example.Gericare.entity.Rol;
import com.example.Gericare.entity.Usuario;
import com.example.Gericare.Repository.UsuarioRepository;
import com.example.Gericare.Service.UsuarioService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List; // <- Añadido para el retorno de List

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario guardar(UsuarioDTO registroDTO) {
        Usuario usuario = new Usuario();
        usuario.setNombre(registroDTO.getNombre());
        usuario.setApellido(registroDTO.getApellido());
        usuario.setCorreoElectronico(registroDTO.getCorreoElectronico());
        usuario.setContrasena(passwordEncoder.encode(registroDTO.getContrasena()));
        Rol rol = rolRepository.findById(registroDTO.getIdRol())
                .orElseThrow(() -> new IllegalStateException("El Rol especificado no existe."));
        usuario.setRolUsuario(rol);
        return usuarioRepository.save(usuario);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreoElectronico(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + username));
        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(usuario.getRolUsuario().getRolNombre().name()));
        return new User(usuario.getCorreoElectronico(), usuario.getContrasena(), authorities);
    }

    // --- MÉTODOS AÑADIDOS PARA SOLUCIONAR EL ERROR ---

    @Override
    public List<UsuarioDTO> listarUsuarios() {
        // Lógica para listar usuarios (pendiente)
        return null;
    }

    @Override
    public UsuarioDTO obtenerUsuarioPorId(Long idUsuario) {
        // Lógica para obtener un usuario (pendiente)
        return null;
    }

    @Override
    public UsuarioDTO crearUsuario(UsuarioDTO usuarioDTO) {
        // Lógica para crear un usuario (pendiente)
        return null;
    }

    @Override
    public UsuarioDTO actualizarUsuario(Long idUsuario, UsuarioDTO usuarioDTO) {
        // Lógica para actualizar un usuario (pendiente)
        return null;
    }

    @Override
    public void eliminarUsuario(Long idUsuario) {
        // Lógica para eliminar un usuario (pendiente)
    }
}