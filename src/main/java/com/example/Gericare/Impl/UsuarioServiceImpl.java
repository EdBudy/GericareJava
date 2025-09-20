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
import org.springframework.security.crypto.password.PasswordEncoder; // Lo inyectaremos más adelante
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service // Servicio
public class UsuarioServiceImpl implements UsuarioService {

    // Inyectamos los repositorios y el codificador de contraseñas que necesitaremos
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    // Usamos inyección por constructor (la mejor práctica)
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario guardar(UsuarioDTO registroDTO) {
        // Lógica para registrar un usuario nuevo
        Usuario usuario = new Usuario();
        usuario.setNombre(registroDTO.getNombre());
        usuario.setApellido(registroDTO.getApellido());
        usuario.setCorreoElectronico(registroDTO.getCorreoElectronico());

        // ¡Importante! Codificamos la contraseña antes de guardarla
        usuario.setContrasena(passwordEncoder.encode(registroDTO.getContrasena()));

        // Buscamos el rol en la base de datos y lo asignamos al usuario
        Rol rol = rolRepository.findById(registroDTO.getIdRol())
                .orElseThrow(() -> new IllegalStateException("El Rol especificado no existe."));
        usuario.setRolUsuario(rol);

        // Aquí deberías establecer el resto de campos que vienen del DTO
        // ... tipoDocumento, documentoIdentificacion, estado, etc.

        return usuarioRepository.save(usuario);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Este método es para que Spring Security busque al usuario por su correo
        Usuario usuario = usuarioRepository.findByCorreoElectronico(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + username));

        // Creamos una colección de "authorities" (roles) para Spring Security
        // Como ahora solo es un rol, es más simple
        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(usuario.getRolUsuario().getRolNombre().name()));

        return new User(usuario.getCorreoElectronico(), usuario.getContrasena(), authorities);
    }
}