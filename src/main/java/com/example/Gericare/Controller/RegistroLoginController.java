package com.example.Gericare.Controller;

import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.entity.Familiar; // ¡Importante! Ahora trabajamos con la entidad Familiar.
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

@Controller
@RequestMapping // Manejo rutas raíz como /login y /registro.
public class RegistroLoginController {

    private final UsuarioService usuarioService;

    public RegistroLoginController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Login

    @GetMapping("/login")
    public String mostrarFormularioDeLogin() {
        // Esto solo muestra la página de login
        return "login";
    }

    // Registro familiares

    @GetMapping("/registro")
    public String mostrarFormularioDeRegistro(Model model) {
        // En lugar de un DTO genérico se prepara un objeto Familiar vacío
        // El formulario de la vista (HTML) se llenará con los datos de este objeto
        model.addAttribute("familiar", new Familiar());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarCuentaDeFamiliar(@Valid @ModelAttribute("familiar") Familiar familiar, BindingResult bindingResult) {
        // Si hay errores de validación, regresa al formulario
        if (bindingResult.hasErrors()) {
            return "registro"; // Devuelve al usuario al formulario para que corrija los errores
        }

        try {
            usuarioService.crearFamiliar(familiar);
            return "redirect:/login?registroExitoso";
        } catch (Exception e) {
            return "redirect:/registro?error";
        }
    }

    // Página principal después del login

    @GetMapping("/")
    public String verPaginaDeInicio() {
        // Redirigir la página raíz a la página de login
        return "redirect:/login";

    }
}
