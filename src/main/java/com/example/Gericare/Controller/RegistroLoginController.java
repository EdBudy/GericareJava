package com.example.Gericare.Controller;

import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.entity.Familiar; // ¡Importante! Ahora trabajamos con la entidad Familiar.
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        return "publico/login";
    }

    // Registro familiares

    @GetMapping("/registro")
    public String mostrarFormularioDeRegistro(Model model) {
        // En lugar de un DTO genérico se prepara un objeto Familiar vacío
        // El formulario de la vista (HTML) se llenará con los datos de este objeto
        model.addAttribute("familiar", new Familiar());
        return "publico/registro";
    }

    @PostMapping("/registro")
    public String registrarCuentaDeFamiliar(@ModelAttribute("familiar") Familiar familiar) {
        // Recibir el objeto Familiar ya con los datos del formulario
        // Llamar al método específico y correcto en nuestro servicio
        try {
            usuarioService.crearFamiliar(familiar);
            // Si el registro es exitoso, redirigir a la página de login con un mensaje de éxito
            return "redirect:/login?registroExitoso";
        } catch (Exception e) {
            // Si no redirigir de vuelta al formulario de registro con un mensaje de error
            return "redirect:/registro?error";
        }
    }

    // Página principal después del login

    @GetMapping("/")
    public String verPaginaDeInicio() {
        // Pag principal a la que se accede después del login
        return "index";
    }
}