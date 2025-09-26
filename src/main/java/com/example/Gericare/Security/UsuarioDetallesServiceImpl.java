package com.example.Gericare.Security;

import com.example.Gericare.Repository.UsuarioRepository;
import com.example.Gericare.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class UsuarioDetallesServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correoElectronico) throws UsernameNotFoundException {
        // Buscamos al usuario por su correo electrónico en la base de datos
        Usuario usuario = usuarioRepository.findByCorreoElectronico(correoElectronico)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró usuario con el correo: " + correoElectronico));

        // Creamos un conjunto de "permisos" (roles) para Spring Security
        Set<GrantedAuthority> authorities = new HashSet<>();
        // Añadir el prefijo "ROLE_" para seguir la convención de Spring Security.
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getRolNombre().name()));

        // Devolvemos un objeto "User" que Spring Security entiende,
        // con el correo, la contraseña (ya encriptada) y sus roles.
        return new User(usuario.getCorreoElectronico(), usuario.getContrasena(), authorities);
    }
}
