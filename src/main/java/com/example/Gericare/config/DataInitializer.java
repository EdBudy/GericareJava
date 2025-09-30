package com.example.Gericare.config;

import com.example.Gericare.Repository.RolRepository;
import com.example.Gericare.Repository.UsuarioRepository;
import com.example.Gericare.entity.*;
import com.example.Gericare.enums.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(RolRepository rolRepository,
                                          UsuarioRepository usuarioRepository,
                                          PasswordEncoder passwordEncoder,
                                          com.example.Gericare.Repository.PacienteRepository pacienteRepository,
                                          com.example.Gericare.Service.PacienteAsignadoService pacienteAsignadoService) {
        return args -> {
            // Crear todos los roles definidos en el enum RolNombre si no existen
            for (RolNombre rolNombre : RolNombre.values()) {
                if (!rolRepository.findByRolNombre(rolNombre).isPresent()) {
                    Rol nuevoRol = new Rol();
                    nuevoRol.setRolNombre(rolNombre);
                    nuevoRol.setDescripcion("Rol de " + rolNombre.name());
                    rolRepository.save(nuevoRol);
                    System.out.println("Rol por defecto creado: " + rolNombre.name());
                }
            }

            // Crear usuario administrador por defecto si no existe
            if (!usuarioRepository.findByCorreoElectronico("admin@gericare.com").isPresent()) {
                // Nos aseguramos de que el rol Administrador exista antes de asignarlo
                Rol rolAdmin = rolRepository.findByRolNombre(RolNombre.Administrador)
                        .orElseThrow(() -> new RuntimeException("Error: Rol Administrador no encontrado."));

                Administrador admin = new Administrador();

                // Atributos de Usuario
                admin.setTipoDocumento(TipoDocumento.CC);
                admin.setDocumentoIdentificacion("123456789");
                admin.setNombre("Admin");
                admin.setApellido("Gericare");
                admin.setDireccion("Calle Falsa 123");
                admin.setCorreoElectronico("admin@gericare.com");
                admin.setContrasena(passwordEncoder.encode("admin123")); // Contraseña por defecto
                admin.setRol(rolAdmin);

                Telefono adminTelefono = new Telefono();
                adminTelefono.setNumero("111111111");
                adminTelefono.setUsuario(admin);
                admin.setTelefonos(Collections.singletonList(adminTelefono));

                // Atributos de Empleado
                admin.setFechaContratacion(LocalDate.now());
                admin.setTipoContrato("Indefinido");
                admin.setContactoEmergencia("987654321");
                admin.setFechaNacimiento(LocalDate.of(1990, 1, 1));

                usuarioRepository.save(admin);
                System.out.println("Usuario administrador por defecto creado.");
            }

            // Crear usuario cuidador por defecto si no existe
            if (!usuarioRepository.findByCorreoElectronico("cuidador@gericare.com").isPresent()) {
                Rol rolCuidador = rolRepository.findByRolNombre(RolNombre.Cuidador)
                        .orElseThrow(() -> new RuntimeException("Error: Rol Cuidador no encontrado."));

                Cuidador cuidador = new Cuidador();

                // Atributos de Usuario
                cuidador.setTipoDocumento(TipoDocumento.CC);
                cuidador.setDocumentoIdentificacion("987654321");
                cuidador.setNombre("Cuidador");
                cuidador.setApellido("Ejemplo");
                cuidador.setDireccion("Avenida Siempre Viva 742");
                cuidador.setCorreoElectronico("cuidador@gericare.com");
                cuidador.setContrasena(passwordEncoder.encode("cuidador123"));
                cuidador.setRol(rolCuidador);

                Telefono cuidadorTelefono = new Telefono();
                cuidadorTelefono.setNumero("222222222");
                cuidadorTelefono.setUsuario(cuidador);
                cuidador.setTelefonos(Collections.singletonList(cuidadorTelefono));

                // Atributos de Empleado
                cuidador.setFechaContratacion(LocalDate.now());
                cuidador.setTipoContrato("Por Horas");
                cuidador.setContactoEmergencia("123456789");
                cuidador.setFechaNacimiento(LocalDate.of(1995, 5, 15));

                usuarioRepository.save(cuidador);
                System.out.println("Usuario cuidador por defecto creado.");
            }

            // Crear usuario familiar por defecto si no existe
            if (!usuarioRepository.findByCorreoElectronico("familiar@gericare.com").isPresent()) {
                Rol rolFamiliar = rolRepository.findByRolNombre(RolNombre.Familiar)
                        .orElseThrow(() -> new RuntimeException("Error: Rol Familiar no encontrado."));

                Familiar familiar = new Familiar();

                // Atributos de Usuario
                familiar.setTipoDocumento(TipoDocumento.CC);
                familiar.setDocumentoIdentificacion("1122334455");
                familiar.setNombre("Familiar");
                familiar.setApellido("Prueba");
                familiar.setDireccion("Calle de la Rosa 45");
                familiar.setCorreoElectronico("familiar@gericare.com");
                familiar.setContrasena(passwordEncoder.encode("familiar123"));
                familiar.setRol(rolFamiliar);

                Telefono familiarTelefono = new Telefono();
                familiarTelefono.setNumero("333333333");
                familiarTelefono.setUsuario(familiar);
                familiar.setTelefonos(Collections.singletonList(familiarTelefono));

                // Atributos de Familiar
                familiar.setParentesco("Hijo/a");

                usuarioRepository.save(familiar);
                System.out.println("Usuario familiar por defecto creado.");
            }

            // Verifica si el paciente de prueba ya existe usando su documento
            if (pacienteRepository.findByDocumentoIdentificacion("12345").isEmpty()) {
                System.out.println("Creando paciente y asignación por defecto...");

                // Busca los usuarios que acabamos de crear para obtener sus IDs
                Usuario admin = usuarioRepository.findByCorreoElectronico("admin@gericare.com").get();
                Usuario cuidador = usuarioRepository.findByCorreoElectronico("cuidador@gericare.com").get();
                Usuario familiar = usuarioRepository.findByCorreoElectronico("familiar@gericare.com").get();

                // Crea el nuevo paciente
                Paciente paciente = new Paciente();
                paciente.setDocumentoIdentificacion("12345");
                paciente.setNombre("Paciente");
                paciente.setApellido("Prueba");
                paciente.setFechaNacimiento(LocalDate.of(1940, 10, 20));
                paciente.setGenero(Genero.Femenino);
                paciente.setContactoEmergencia("3001234567");
                paciente.setEstadoCivil("Viudo(a)");
                paciente.setTipoSangre(TipoSangre.O_POSITIVO);
                paciente.setEstado(EstadoPaciente.Activo);

                Paciente pacienteGuardado = pacienteRepository.save(paciente);

                // Crea la asignación
                pacienteAsignadoService.crearAsignacion(
                        pacienteGuardado.getIdPaciente(),
                        cuidador.getIdUsuario(),
                        familiar.getIdUsuario(),
                        admin.getIdUsuario()
                );

                System.out.println("Paciente y asignación por defecto creados con éxito.");
            }
        };
    }
}
