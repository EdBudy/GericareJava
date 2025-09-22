package com.example.Gericare.Impl;

import com.example.Gericare.Repository.RolRepository;
import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.Repository.UsuarioRepository;
import com.example.Gericare.entity.*;
import com.example.Gericare.enums.EstadoUsuario;
import com.example.Gericare.enums.RolNombre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository; // Necesario para asignar roles

    @Autowired
    private PasswordEncoder passwordEncoder; // Inyectar la herramienta para encriptar

    // Metodos de creacion

    public Cuidador crearCuidador(Cuidador cuidador) {
        // Encriptar la contraseña antes de guardarla
        cuidador.setContrasena(passwordEncoder.encode(cuidador.getContrasena()));

        // Buscar y asignar el rol de "CUIDADOR"
        Rol rolCuidador = rolRepository.findByRolNombre(RolNombre.Cuidador)
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
        cuidador.setRol(rolCuidador);

        // Guardar el nuevo cuidador en la base de datos
        return usuarioRepository.save(cuidador);
    }

    public Familiar crearFamiliar(Familiar familiar) {
        // Validar lo de los 3 teléfonos
        if (familiar.getTelefonos() != null && familiar.getTelefonos().size() > 3) {
            throw new IllegalStateException("Un familiar no puede tener más de 3 teléfonos.");
        }

        // Encriptar la contraseña
        familiar.setContrasena(passwordEncoder.encode(familiar.getContrasena()));

        // Asignar el rol de "FAMILIAR"
        Rol rolFamiliar = rolRepository.findByRolNombre(RolNombre.Familiar)
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
        familiar.setRol(rolFamiliar);

        // Asegurar que la relación bidireccional se establezca
        if (familiar.getTelefonos() != null) {
            familiar.getTelefonos().forEach(telefono -> telefono.setUsuario(familiar));
        }

        // Guardar el nuevo familiar
        return usuarioRepository.save(familiar);
    }

    // Metodos de consulta

    @Override
    public List<Usuario> listarTodosLosUsuarios() {
        // Debido a @Where esto solo trae los usuarios 'Activos'
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    // Borrador logico
    @Override
    public void eliminarUsuario(Long id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setEstado(EstadoUsuario.Inactivo); // Cambiar estado
            usuarioRepository.save(usuario); // Guardar cambio
        });
    }

    @Override
    public Usuario actualizarUsuario(Long id, Usuario detallesUsuario) {
        // Esta lógica necesita ser más detallada pero la base esta
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombre(detallesUsuario.getNombre());
            usuario.setApellido(detallesUsuario.getApellido());
            usuario.setDireccion(detallesUsuario.getDireccion());
            // No se suele actualizar la contraseña en un método de "actualizar perfil" general
            // ... otros campos comunes
            return usuarioRepository.save(usuario);
        }).orElse(null);
    }
}