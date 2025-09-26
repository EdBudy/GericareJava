package com.example.Gericare.Impl;

import com.example.Gericare.DTO.EmpleadoDTO;
import com.example.Gericare.DTO.FamiliarDTO;
import com.example.Gericare.DTO.UsuarioDTO;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    // Inyectar las dependencias necesarias.
    // Son las herramientas que el servicio necesita para trabajar.
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Métodos de creación

    @Override
    public UsuarioDTO crearCuidador(Cuidador cuidador) {
        // Encriptar la contraseña para no guardarla en texto plano.
        cuidador.setContrasena(passwordEncoder.encode(cuidador.getContrasena()));

        // Buscar y asignar el rol correspondiente desde la base de datos.
        Rol rolCuidador = rolRepository.findByRolNombre(RolNombre.Cuidador)
                .orElseThrow(() -> new RuntimeException("Error: Rol 'Cuidador' no encontrado."));
        cuidador.setRol(rolCuidador);

        // Persistir la entidad en la base de datos.
        Cuidador cuidadorGuardado = usuarioRepository.save(cuidador);

        // Convertir la entidad guardada a un DTO para devolver solo datos seguros.
        return toDTO(cuidadorGuardado);
    }

    @Override
    public UsuarioDTO crearFamiliar(Familiar familiar) {
        // Aplicar la regla de negocio: no más de 3 teléfonos por familiar.
        if (familiar.getTelefonos() != null && familiar.getTelefonos().size() > 3) {
            throw new IllegalStateException("Un familiar no puede tener más de 3 teléfonos.");
        }

        // Encriptar la contraseña.
        familiar.setContrasena(passwordEncoder.encode(familiar.getContrasena()));

        // Asignar el rol de Familiar.
        Rol rolFamiliar = rolRepository.findByRolNombre(RolNombre.Familiar)
                .orElseThrow(() -> new RuntimeException("Error: Rol 'Familiar' no encontrado."));
        familiar.setRol(rolFamiliar);

        // Establecer la relación bidireccional entre el familiar y sus teléfonos.
        if (familiar.getTelefonos() != null) {
            familiar.getTelefonos().forEach(telefono -> telefono.setUsuario(familiar));
        }

        // Persistir la entidad familiar.
        Familiar familiarGuardado = usuarioRepository.save(familiar);

        // Convertir y devolver el DTO.
        return toDTO(familiarGuardado);
    }

    // Métodos de consulta

    @Override
    public List<UsuarioDTO> listarTodosLosUsuarios() {
        // Obtener todas las entidades de la base de datos.
        // La anotación @Where en la entidad Usuario ya filtra automáticamente por estado 'Activo'.
        return usuarioRepository.findAll()
                .stream()
                // Convertir cada entidad encontrada a su DTO correspondiente.
                .map(this::toDTO)
                // Recolectar los resultados en una lista.
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UsuarioDTO> obtenerUsuarioPorId(Long id) {
        // Buscar un usuario por su ID. Devuelve un Optional para manejar si no se encuentra.
        return usuarioRepository.findById(id)
                // Si el Optional contiene un usuario, convertirlo a DTO.
                .map(this::toDTO);
    }

    // Métodos de gestión

    @Override
    public void eliminarUsuario(Long id) {
        // Realizar un borrado lógico, no físico.
        // Buscar el usuario y, si existe, cambiar su estado.
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setEstado(EstadoUsuario.Inactivo);
            // Guardar la entidad actualizada con el nuevo estado.
            usuarioRepository.save(usuario);
        });
    }

    @Override
    public Optional<UsuarioDTO> actualizarUsuario(Long id, UsuarioDTO usuarioDTO) {
        // Buscar el usuario a actualizar.
        return usuarioRepository.findById(id).map(usuarioExistente -> {
            // Actualizar solo los campos permitidos desde el DTO.
            usuarioExistente.setNombre(usuarioDTO.getNombre());
            usuarioExistente.setApellido(usuarioDTO.getApellido());
            usuarioExistente.setDireccion(usuarioDTO.getDireccion());
            // Guardar los cambios en la base de datos.
            Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
            // Devolver el resultado como un DTO.
            return toDTO(usuarioActualizado);
        });
    }

    @Override
    public UsuarioDTO crearAdministrador(Administrador administrador) {
        // Encriptar la contraseña para seguridad.
        administrador.setContrasena(passwordEncoder.encode(administrador.getContrasena()));

        // Buscar y asignar el rol de Administrador.
        Rol rolAdmin = rolRepository.findByRolNombre(RolNombre.Administrador)
                .orElseThrow(() -> new RuntimeException("Error: Rol 'Administrador' no encontrado."));
        administrador.setRol(rolAdmin);

        // Guardar la nueva entidad en la base de datos.
        Administrador adminGuardado = usuarioRepository.save(administrador);

        // Convertir y devolver el DTO.
        return toDTO(adminGuardado);
    }



    // Métodos privados para traducción (entidad a DTO)
    // Esta es la lógica que convierte los objetos de la base de datos (Entidades)
    // en objetos seguros para mostrar (DTOs).

    private UsuarioDTO toDTO(Usuario usuario) {
        // Determinar el tipo específico de usuario para usar el DTO correcto.
        if (usuario instanceof Empleado) {
            return toEmpleadoDTO((Empleado) usuario);
        }
        if (usuario instanceof Familiar) {
            return toFamiliarDTO((Familiar) usuario);
        }

        // Si el código llega aquí, es un estado inesperado.
        // Lanzar excepción
        throw new IllegalArgumentException("Tipo de usuario desconocido: " + usuario.getClass().getName());
    }

    private EmpleadoDTO toEmpleadoDTO(Empleado empleado) {
        // Crear un DTO de Empleado y poblarlo con datos de la entidad.
        EmpleadoDTO dto = new EmpleadoDTO();
        // Poblar datos comunes heredados de Usuario.
        dto.setIdUsuario(empleado.getIdUsuario());
        dto.setTipoDocumento(empleado.getTipoDocumento());
        dto.setDocumentoIdentificacion(empleado.getDocumentoIdentificacion());
        dto.setNombre(empleado.getNombre());
        dto.setApellido(empleado.getApellido());
        dto.setDireccion(empleado.getDireccion());
        dto.setCorreoElectronico(empleado.getCorreoElectronico());
        // Poblar datos específicos de Empleado.
        dto.setFechaContratacion(empleado.getFechaContratacion());
        dto.setTipoContrato(empleado.getTipoContrato());
        dto.setContactoEmergencia(empleado.getContactoEmergencia());
        dto.setFechaNacimiento(empleado.getFechaNacimiento());
        return dto;
    }

    private FamiliarDTO toFamiliarDTO(Familiar familiar) {
        // Crear un DTO de Familiar y poblarlo con datos de la entidad.
        FamiliarDTO dto = new FamiliarDTO();
        // Poblar datos comunes heredados de Usuario.
        dto.setIdUsuario(familiar.getIdUsuario());
        dto.setTipoDocumento(familiar.getTipoDocumento());
        dto.setDocumentoIdentificacion(familiar.getDocumentoIdentificacion());
        dto.setNombre(familiar.getNombre());
        dto.setApellido(familiar.getApellido());
        dto.setDireccion(familiar.getDireccion());
        dto.setCorreoElectronico(familiar.getCorreoElectronico());
        // Poblar el dato específico de Familiar.
        dto.setParentesco(familiar.getParentesco());
        return dto;
    }
}