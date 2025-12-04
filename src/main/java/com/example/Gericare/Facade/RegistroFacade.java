package com.example.Gericare.Facade;

import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Entity.Familiar;
import com.example.Gericare.Service.EmailService;
import com.example.Gericare.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegistroFacade {

    private final UsuarioService usuarioService;
    private final EmailService emailService;

    @Autowired
    public RegistroFacade(UsuarioService usuarioService, EmailService emailService) {
        this.usuarioService = usuarioService;
        this.emailService = emailService;
    }

    // Encapsula la complejidad de guardar usuario y notificar (si correo falla usuario igual queda registrado)

    public void registrarFamiliar(Familiar familiar) {
        // Subsistema 1 guardar usuario en BD
        UsuarioDTO nuevoUsuario = usuarioService.crearFamiliar(familiar);

        // Subsistema 2 enviar correo bienvenida
        try {
            System.out.println("Facade: Iniciando envío de correo a " + nuevoUsuario.getCorreoElectronico());

            emailService.sendWelcomeEmail(
                    nuevoUsuario.getCorreoElectronico(),
                    nuevoUsuario.getNombre() + " " + nuevoUsuario.getApellido(),
                    nuevoUsuario.getDocumentoIdentificacion()
            );

            System.out.println("Facade: Correo enviado correctamente.");
        } catch (Exception e) {
            // Loguea el error, pero no lanza excepción pa no romper registro
            System.err.println("Facade Error: El usuario se creó, pero falló el envío del correo: " + e.getMessage());
        }
    }
}