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

@Service // Servicio gestionado por Spring

// Permite a Spring Security obtener los datos del usuario para autenticación
public class UsuarioDetallesServiceImpl implements UserDetailsService {
    // UsuarioDetallesServiceImpl es como un traductor (Spring Security solo entiende un formato de info:
    // un objeto org.springframework.security.core.userdetails.User)

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correoElectronico) throws UsernameNotFoundException {
        // Busca al usuario en la base de datos por su correo en la bd
        Usuario usuario = usuarioRepository.findByCorreoElectronico(correoElectronico)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró usuario con el correo: " + correoElectronico));

        // Crea un conjunto de roles/permisos
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getRolNombre().name()));

        // Devuelve un objeto "User" que Spring Security entiende,
        // con correo, contraseña (hasheada) y sus roles
        return new User(usuario.getCorreoElectronico(), usuario.getContrasena(), authorities);
    }
}
