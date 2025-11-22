package com.example.Gericare.Controller;

import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.Entity.Familiar;
import com.example.Gericare.Entity.Telefono;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class RegistroLoginController {

    private final UsuarioService usuarioService;

    public RegistroLoginController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // RegistroLoginController
    @GetMapping("/login")
    public String mostrarFormularioDeLogin() {
        return "auth/login";
    }
    // Cuando se accede a la URL /login el controlador muestra la página "login.html"
    // "login.html" envía los datos por método POST que son procesados por Spring Security

    @GetMapping("/registro")
    public String mostrarFormularioDeRegistro(Model model) {
        model.addAttribute("familiar", new Familiar());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrarCuentaDeFamiliar(@Valid @ModelAttribute("familiar") Familiar familiar, BindingResult bindingResult, Model model) {

        // Contraseña = Documento de Identidad
        // Se asigna antes de procesar el guardado para que viaje al servicio
        if (familiar.getDocumentoIdentificacion() != null && !familiar.getDocumentoIdentificacion().isEmpty()) {
            familiar.setContrasena(familiar.getDocumentoIdentificacion());
        }

        // Limpieza de teléfonos
        if (familiar.getTelefonos() != null) {
            List<Telefono> telefonosNoVacios = familiar.getTelefonos().stream()
                    .filter(t -> t.getNumero() != null && !t.getNumero().trim().isEmpty())
                    .collect(Collectors.toList());
            familiar.setTelefonos(telefonosNoVacios);
        }

        // Validación de errores del formulario
        if (bindingResult.hasErrors()) {
            return "auth/registro";
        }

        try {
            // Llamada al servicio (Crear y enviar correo)
            usuarioService.crearFamiliar(familiar);

            return "redirect:/login?registroExitoso";

        } catch (Exception e) {
            model.addAttribute("familiar", familiar);
            model.addAttribute("error", "Ya existe un usuario con el mismo documento o correo electrónico.");
            System.err.println("Error en registro: " + e.getMessage());
            return "auth/registro";
        }
    }

    @GetMapping("/")
    public String verPaginaDeInicio() {
        return "redirect:/login";
    }
}