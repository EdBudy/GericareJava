package com.example.Gericare.config;

import com.example.Gericare.Repository.*;
import com.example.Gericare.Entity.*;
import com.example.Gericare.Enums.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.Gericare.Enums.TipoContrato;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(RolRepository rolRepository,
                                          UsuarioRepository usuarioRepository,
                                          PasswordEncoder passwordEncoder,
                                          com.example.Gericare.Repository.PacienteRepository pacienteRepository,
                                          com.example.Gericare.Service.PacienteAsignadoService pacienteAsignadoService,
                                          ActividadRepository actividadRepository, SolicitudRepository solicitudRepository,
                                          TratamientoRepository tratamientoRepository) {
        return args -> {
            // Crear roles
            for (RolNombre rolNombre : RolNombre.values()) {
                if (rolRepository.findByRolNombre(rolNombre).isEmpty()) {
                    Rol nuevoRol = new Rol();
                    nuevoRol.setRolNombre(rolNombre);
                    nuevoRol.setDescripcion("Rol de " + rolNombre.name());
                    rolRepository.save(nuevoRol);
                    System.out.println("Rol por defecto creado: " + rolNombre.name());
                }
            }

            // Nombres y apellidos cuidadores
            String[] nombresCuidadores = {"Ana Milena", "Carlos Alberto", "Sofia Isabel"};
            String[] apellidosCuidadores = {"Rojas", "Velez", "Castro"};

            // Nombres y apellidos familiares
            String[] nombresFamiliares = {"Juan David", "Maria Camila", "Pedro Jose"};
            String[] apellidosFamiliares = {"Herrera", "Vargas", "Ramirez"};


            // Crear admin
            if (usuarioRepository.findByCorreoElectronico("admin@gericare.com").isEmpty()) {
                Rol rolAdmin = rolRepository.findByRolNombre(RolNombre.Administrador).orElseThrow(() -> new RuntimeException("Rol no encontrado"));
                Administrador admin = new Administrador();
                admin.setTipoDocumento(TipoDocumento.CC);
                admin.setDocumentoIdentificacion("123456789");
                admin.setNombre("Admin");
                admin.setApellido("Gericare");
                admin.setDireccion("Calle 85 #15-20");
                admin.setCorreoElectronico("admin@gericare.com");
                admin.setContrasena(passwordEncoder.encode("admin123"));
                admin.setRol(rolAdmin);
                admin.setEstado(EstadoUsuario.Activo);
                admin.setNecesitaCambioContrasena(false);
                Telefono adminTelefono = new Telefono();
                adminTelefono.setNumero("3131234567");
                adminTelefono.setUsuario(admin);
                admin.setTelefonos(Collections.singletonList(adminTelefono));
                admin.setFechaContratacion(LocalDate.now());
                admin.setTipoContrato(TipoContrato.TERMINO_INDEFINIDO);
                admin.setContactoEmergencia("3131234567");
                admin.setFechaNacimiento(LocalDate.of(1990, 1, 1));
                usuarioRepository.save(admin);
                System.out.println("Usuario administrador por defecto creado.");
            }

            // Cuidadores
            for (int i = 0; i < 3; i++) {
                int userIndex = i + 1;
                if (usuarioRepository.findByCorreoElectronico("cuidador_" + userIndex + "@gericare.com").isEmpty()) {
                    Rol rolCuidador = rolRepository.findByRolNombre(RolNombre.Cuidador).orElseThrow(() -> new RuntimeException("Rol no encontrado"));
                    Cuidador cuidador = new Cuidador();
                    cuidador.setTipoDocumento(TipoDocumento.CC);
                    cuidador.setDocumentoIdentificacion("98765432" + userIndex);
                    cuidador.setNombre(nombresCuidadores[i]);
                    cuidador.setApellido(apellidosCuidadores[i]);
                    cuidador.setDireccion("Calle 72 #10-4" + userIndex);
                    cuidador.setCorreoElectronico("cuidador_" + userIndex + "@gericare.com");
                    cuidador.setContrasena(passwordEncoder.encode("cuidador" + userIndex));
                    cuidador.setRol(rolCuidador);
                    cuidador.setEstado(EstadoUsuario.Activo);
                    cuidador.setNecesitaCambioContrasena(false);
                    Telefono cuidadorTelefono = new Telefono();
                    cuidadorTelefono.setNumero("320987654" + userIndex);
                    cuidadorTelefono.setUsuario(cuidador);
                    cuidador.setTelefonos(Collections.singletonList(cuidadorTelefono));
                    cuidador.setFechaContratacion(LocalDate.now());
                    cuidador.setTipoContrato(TipoContrato.PRESTACION_DE_SERVICIOS);
                    cuidador.setContactoEmergencia("313876543" + userIndex);
                    cuidador.setFechaNacimiento(LocalDate.of(1995, 5, 15));
                    usuarioRepository.save(cuidador);
                    System.out.println("Usuario cuidador por defecto creado: cuidador_" + userIndex);
                }
            }

            // Familiares
            for (int i = 0; i < 3; i++) {
                int userIndex = i + 1;
                if (usuarioRepository.findByCorreoElectronico("familiar_" + userIndex + "@gmail.com").isEmpty()) {
                    Rol rolFamiliar = rolRepository.findByRolNombre(RolNombre.Familiar).orElseThrow(() -> new RuntimeException("Rol no encontrado"));
                    Familiar familiar = new Familiar();
                    familiar.setTipoDocumento(TipoDocumento.CC);
                    familiar.setDocumentoIdentificacion("11223344" + (5 + userIndex));
                    familiar.setNombre(nombresFamiliares[i]);
                    familiar.setApellido(apellidosFamiliares[i]);
                    familiar.setDireccion("Calle 72 #11-7" + userIndex);
                    familiar.setCorreoElectronico("familiar_" + userIndex + "@gmail.com");
                    familiar.setContrasena(passwordEncoder.encode("familiar" + userIndex));
                    familiar.setRol(rolFamiliar);
                    familiar.setEstado(EstadoUsuario.Activo);
                    familiar.setNecesitaCambioContrasena(false);
                    Telefono familiarTelefono = new Telefono();
                    familiarTelefono.setNumero("313654321" + userIndex);
                    familiarTelefono.setUsuario(familiar);
                    familiar.setTelefonos(Collections.singletonList(familiarTelefono));
                    familiar.setParentesco("Hijo/a");
                    usuarioRepository.save(familiar);
                    System.out.println("Usuario familiar por defecto creado: familiar_" + userIndex);
                }
            }

            // Pacientes y asignaciones
            Administrador admin = (Administrador) usuarioRepository.findByCorreoElectronico("admin@gericare.com").get();

            if (pacienteRepository.findByDocumentoIdentificacion("12345").isEmpty()) {
                Usuario cuidador1 = usuarioRepository.findByCorreoElectronico("cuidador_1@gericare.com").get();
                Usuario familiar1 = usuarioRepository.findByCorreoElectronico("familiar_1@gmail.com").get();

                Paciente paciente1 = new Paciente();
                paciente1.setDocumentoIdentificacion("12345");
                paciente1.setNombre("Roberto");
                paciente1.setApellido("Gómez");
                paciente1.setFechaNacimiento(LocalDate.of(1940, 10, 20));
                paciente1.setGenero(Genero.Masculino);
                paciente1.setContactoEmergencia("3001234567");
                paciente1.setEstadoCivil("Viudo(a)");
                paciente1.setTipoSangre(TipoSangre.O_POSITIVO);
                paciente1.setEstado(EstadoPaciente.Activo);
                paciente1.setSeguroMedico("Famisanar");
                paciente1.setNumeroSeguro("80012345-01");
                Paciente pacienteGuardado1 = pacienteRepository.save(paciente1);

                pacienteAsignadoService.crearAsignacion(pacienteGuardado1.getIdPaciente(), cuidador1.getIdUsuario(), familiar1.getIdUsuario(), admin.getIdUsuario());
                System.out.println("Paciente 1 y asignación creados.");
            }

            if (pacienteRepository.findByDocumentoIdentificacion("67890").isEmpty()) {
                Usuario cuidador2 = usuarioRepository.findByCorreoElectronico("cuidador_2@gericare.com").get();
                Usuario familiar2 = usuarioRepository.findByCorreoElectronico("familiar_2@gmail.com").get();

                Paciente paciente2 = new Paciente();
                paciente2.setDocumentoIdentificacion("67890");
                paciente2.setNombre("María");
                paciente2.setApellido("Fernández");
                paciente2.setFechaNacimiento(LocalDate.of(1952, 5, 15));
                paciente2.setGenero(Genero.Femenino);
                paciente2.setContactoEmergencia("3001234568");
                paciente2.setEstadoCivil("Casado(a)");
                paciente2.setTipoSangre(TipoSangre.A_NEGATIVO);
                paciente2.setEstado(EstadoPaciente.Activo);
                paciente2.setSeguroMedico("Sura EPS");
                paciente2.setNumeroSeguro("90056789-02");
                Paciente pacienteGuardado2 = pacienteRepository.save(paciente2);

                pacienteAsignadoService.crearAsignacion(pacienteGuardado2.getIdPaciente(), cuidador2.getIdUsuario(), familiar2.getIdUsuario(), admin.getIdUsuario());
                System.out.println("Paciente 2 y asignación creados.");
            }

            if (pacienteRepository.findByDocumentoIdentificacion("13579").isEmpty()) {
                Usuario cuidador3 = usuarioRepository.findByCorreoElectronico("cuidador_3@gericare.com").get();

                Paciente paciente3 = new Paciente();
                paciente3.setDocumentoIdentificacion("13579");
                paciente3.setNombre("Carlos");
                paciente3.setApellido("Sánchez");
                paciente3.setFechaNacimiento(LocalDate.of(1948, 2, 10));
                paciente3.setGenero(Genero.Masculino);
                paciente3.setContactoEmergencia("3001234569");
                paciente3.setEstadoCivil("Soltero(a)");
                paciente3.setTipoSangre(TipoSangre.B_POSITIVO);
                paciente3.setEstado(EstadoPaciente.Activo);
                paciente3.setSeguroMedico("Compensar EPS");
                paciente3.setNumeroSeguro("86098765-03");
                Paciente pacienteGuardado3 = pacienteRepository.save(paciente3);

                pacienteAsignadoService.crearAsignacion(pacienteGuardado3.getIdPaciente(), cuidador3.getIdUsuario(), null, admin.getIdUsuario());
                System.out.println("Paciente 3 y asignación creados (sin familiar).");
            }

            if (pacienteRepository.findByDocumentoIdentificacion("54321").isEmpty()) {
                Usuario cuidador1 = usuarioRepository.findByCorreoElectronico("cuidador_1@gericare.com").get();
                Usuario familiar3 = usuarioRepository.findByCorreoElectronico("familiar_3@gmail.com").get();
                Paciente p = new Paciente();
                p.setDocumentoIdentificacion("54321");
                p.setNombre("Laura");
                p.setApellido("Torres");
                p.setFechaNacimiento(LocalDate.of(1935, 3, 12));
                p.setGenero(Genero.Femenino);
                p.setContactoEmergencia("3112345678");
                p.setEstadoCivil("Divorciado(a)");
                p.setTipoSangre(TipoSangre.A_POSITIVO);
                p.setEstado(EstadoPaciente.Activo);
                p.setSeguroMedico("Sanitas");
                p.setNumeroSeguro("83012345-04");
                Paciente pg = pacienteRepository.save(p);
                pacienteAsignadoService.crearAsignacion(pg.getIdPaciente(), cuidador1.getIdUsuario(), familiar3.getIdUsuario(), admin.getIdUsuario());
            }
            if (pacienteRepository.findByDocumentoIdentificacion("98765").isEmpty()) {
                Usuario cuidador2 = usuarioRepository.findByCorreoElectronico("cuidador_2@gericare.com").get();
                Usuario familiar1 = usuarioRepository.findByCorreoElectronico("familiar_1@gmail.com").get();
                Paciente p = new Paciente();
                p.setDocumentoIdentificacion("98765");
                p.setNombre("Jorge");
                p.setApellido("Diaz");
                p.setFechaNacimiento(LocalDate.of(1942, 11, 30));
                p.setGenero(Genero.Masculino);
                p.setContactoEmergencia("3123456789");
                p.setEstadoCivil("Viudo(a)");
                p.setTipoSangre(TipoSangre.B_NEGATIVO);
                p.setEstado(EstadoPaciente.Activo);
                p.setSeguroMedico("Nueva EPS");
                p.setNumeroSeguro("90033344-05");
                Paciente pg = pacienteRepository.save(p);
                pacienteAsignadoService.crearAsignacion(pg.getIdPaciente(), cuidador2.getIdUsuario(), familiar1.getIdUsuario(), admin.getIdUsuario());
            }
            if (pacienteRepository.findByDocumentoIdentificacion("24680").isEmpty()) {
                Usuario cuidador3 = usuarioRepository.findByCorreoElectronico("cuidador_3@gericare.com").get();
                Paciente p = new Paciente();
                p.setDocumentoIdentificacion("24680");
                p.setNombre("Lucia");
                p.setApellido("Mora");
                p.setFechaNacimiento(LocalDate.of(1955, 7, 25));
                p.setGenero(Genero.Femenino);
                p.setContactoEmergencia("3134567890");
                p.setEstadoCivil("Soltero(a)");
                p.setTipoSangre(TipoSangre.O_NEGATIVO);
                p.setEstado(EstadoPaciente.Activo);
                p.setSeguroMedico("Salud Total");
                p.setNumeroSeguro("80099988-06");
                Paciente pg = pacienteRepository.save(p);
                pacienteAsignadoService.crearAsignacion(pg.getIdPaciente(), cuidador3.getIdUsuario(), null, admin.getIdUsuario());
            }
            if (pacienteRepository.findByDocumentoIdentificacion("11223").isEmpty()) {
                Usuario cuidador1 = usuarioRepository.findByCorreoElectronico("cuidador_1@gericare.com").get();
                Usuario familiar2 = usuarioRepository.findByCorreoElectronico("familiar_2@gmail.com").get();
                Paciente p = new Paciente();
                p.setDocumentoIdentificacion("11223");
                p.setNombre("Miguel");
                p.setApellido("Angel");
                p.setFechaNacimiento(LocalDate.of(1938, 9, 5));
                p.setGenero(Genero.Masculino);
                p.setContactoEmergencia("3145678901");
                p.setEstadoCivil("Casado(a)");
                p.setTipoSangre(TipoSangre.AB_POSITIVO);
                p.setEstado(EstadoPaciente.Activo);
                p.setSeguroMedico("Coomeva EPS");
                p.setNumeroSeguro("83055566-07");
                Paciente pg = pacienteRepository.save(p);
                pacienteAsignadoService.crearAsignacion(pg.getIdPaciente(), cuidador1.getIdUsuario(), familiar2.getIdUsuario(), admin.getIdUsuario());
            }


            // Actividades
            if (actividadRepository.count() == 0) {
                Paciente p1 = pacienteRepository.findByDocumentoIdentificacion("12345").get();
                Paciente p2 = pacienteRepository.findByDocumentoIdentificacion("67890").get();
                Paciente p3 = pacienteRepository.findByDocumentoIdentificacion("13579").get();
                Paciente p4 = pacienteRepository.findByDocumentoIdentificacion("54321").get();

                actividadRepository.saveAll(List.of(
                        new Actividad(null, p1, admin, "Toma de presión arterial", "Monitoreo de presión", LocalDate.now(), LocalTime.of(8, 0), LocalTime.of(8, 15), EstadoActividad.Pendiente),
                        new Actividad(null, p1, admin, "Caminata ligera", "Caminata de 15 minutos por el jardín", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(10, 15), EstadoActividad.Pendiente),
                        new Actividad(null, p2, admin, "Administrar medicamento", "Entregar pastilla para la tiroides", LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(9, 10), EstadoActividad.Pendiente),
                        new Actividad(null, p3, admin, "Terapia física", "Ejercicios de movilidad para la rodilla", LocalDate.now().plusDays(1), LocalTime.of(11, 0), LocalTime.of(11, 30), EstadoActividad.Pendiente),
                        new Actividad(null, p4, admin, "Control de glucosa", "Medición de azúcar en sangre antes del desayuno", LocalDate.now(), LocalTime.of(7, 30), LocalTime.of(7, 40), EstadoActividad.Pendiente)
                ));
                System.out.println("Actividades por defecto creadas.");
            }

            // solicitud y tratamiento
            if (solicitudRepository.count() == 0 && tratamientoRepository.count() == 0) {
                System.out.println("Creando datos de ejemplo para Solicitudes y Tratamientos...");

                // Obtener usuarios/pacientes necesarios
                Familiar familiar1 = (Familiar) usuarioRepository.findByCorreoElectronico("familiar_1@gmail.com").orElse(null);
                Paciente paciente1 = pacienteRepository.findByDocumentoIdentificacion("12345").orElse(null);
                Paciente paciente2 = pacienteRepository.findByDocumentoIdentificacion("67890").orElse(null);
                Cuidador cuidador1 = (Cuidador) usuarioRepository.findByCorreoElectronico("cuidador_1@gericare.com").orElse(null);
                Cuidador cuidador2 = (Cuidador) usuarioRepository.findByCorreoElectronico("cuidador_2@gericare.com").orElse(null);
                Administrador adminFound = (Administrador) usuarioRepository.findByCorreoElectronico("admin@gericare.com").orElse(null);

                if (familiar1 != null && paciente1 != null && paciente2 != null && cuidador1 != null && cuidador2 != null && adminFound != null) {

                    // Solicitud
                    Solicitud sol1 = new Solicitud();
                    sol1.setFamiliar(familiar1);
                    sol1.setPaciente(paciente1);
                    sol1.setTipoSolicitud(TipoSolicitud.Salida);
                    sol1.setMotivoSolicitud("Solicito salida para el paciente Roberto Gómez el próximo fin de semana.");
                    sol1.setEstadoSolicitud(EstadoSolicitud.Pendiente);
                    solicitudRepository.save(sol1);

                    // Tratamientos
                    Tratamiento trat1 = new Tratamiento();
                    trat1.setPaciente(paciente1);
                    trat1.setAdministrador(adminFound);
                    trat1.setCuidador(cuidador1);
                    trat1.setDescripcion("Administrar Analgésico");
                    trat1.setInstruccionesEspeciales("Dar 1 pastilla después del almuerzo.");
                    trat1.setFechaInicio(LocalDate.now());
                    trat1.setFechaFin(LocalDate.now().plusDays(7));
                    trat1.setEstadoTratamiento(EstadoActividad.Pendiente);
                    tratamientoRepository.save(trat1);

                    Tratamiento trat2 = new Tratamiento();
                    trat2.setPaciente(paciente2);
                    trat2.setAdministrador(adminFound);
                    trat2.setCuidador(cuidador2);
                    trat2.setDescripcion("Control de signos vitales");
                    trat2.setFechaInicio(LocalDate.now());
                    trat2.setEstadoTratamiento(EstadoActividad.Pendiente);
                    trat2.setObservaciones("Paciente estable.");
                    tratamientoRepository.save(trat2);

                    System.out.println("Datos de ejemplo para Solicitud y Tratamiento creados.");
                } else {
                    System.out.println("No se pudieron crear datos de ejemplo para Solicitud/Tratamiento: Faltan usuarios/pacientes base.");
                }
            }
        };
    }
}