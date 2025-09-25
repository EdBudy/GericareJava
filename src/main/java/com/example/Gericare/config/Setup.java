package com.example.Gericare.config;

import com.example.Gericare.Repository.RolRepository;
import com.example.Gericare.Repository.UsuarioRepository;
import com.example.Gericare.entity.Administrador;
import com.example.Gericare.entity.Rol;
import com.example.Gericare.enums.RolNombre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class Setup implements CommandLineRunner {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Esta lógica se ejecutará una sola vez cuando la aplicación arranque.

        // 1. Crear los roles si no existen.
        crearRolSiNoExiste(RolNombre.Administrador);
        crearRolSiNoExiste(RolNombre.Cuidador);
        crearRolSiNoExiste(RolNombre.Familiar);

        // 2. Crear el "Admin Cero" si no existe.
        if (usuarioRepository.findByCorreoElectronico("admin@gericare.com").isEmpty()) {

            // Buscar el rol de Administrador que acabamos de crear.
            Rol rolAdmin = rolRepository.findByRolNombre(RolNombre.Administrador).get();

            // Crear la entidad Administrador.
            Administrador admin = new Administrador();
            admin.setNombre("Admin");
            admin.setApellido("Principal");
            admin.setCorreoElectronico("admin@gericare.com");
            // Encriptar la contraseña usando el PasswordEncoder de la aplicación.
            admin.setContrasena(passwordEncoder.encode("adminpass"));
            admin.setTipoDocumento(com.example.Gericare.enums.TipoDocumento.CC);
            admin.setDocumentoIdentificacion("123456789");
            admin.setDireccion("Oficina Central");
            admin.setFechaContratacion(LocalDate.now());
            admin.setRol(rolAdmin);

            usuarioRepository.save(admin);
            System.out.println(">>> Creado Admin Cero con correo: admin@gericare.com y contraseña: adminpass");
        }
    }

    private void crearRolSiNoExiste(RolNombre nombreRol) {
        if (rolRepository.findByRolNombre(nombreRol).isEmpty()) {
            Rol nuevoRol = new Rol();
            nuevoRol.setRolNombre(nombreRol);
            rolRepository.save(nuevoRol);
        }
    }
}