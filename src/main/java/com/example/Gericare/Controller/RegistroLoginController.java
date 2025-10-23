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
        return "login";
    }
    // Cuando se accede a la URL /login el controlador muestra la página "login.html"
    // "login.html" envía los datos por método POST que son procesados por Spring Security

    @GetMapping("/registro")
    public String mostrarFormularioDeRegistro(Model model) {
        model.addAttribute("familiar", new Familiar());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarCuentaDeFamiliar(@Valid @ModelAttribute("familiar") Familiar familiar, BindingResult bindingResult, Model model) {

        // Limpiar teléfonos vacíos que podrían venir del formulario
        if (familiar.getTelefonos() != null) {
            List<Telefono> telefonosNoVacios = familiar.getTelefonos().stream()
                    .filter(t -> t.getNumero() != null && !t.getNumero().trim().isEmpty())
                    .collect(Collectors.toList());
            familiar.setTelefonos(telefonosNoVacios);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("familiar", familiar); // Devolver el objeto con los errores
            return "registro";
        }

        try {
            usuarioService.crearFamiliar(familiar);
            return "redirect:/login?registroExitoso";
        } catch (Exception e) {
            model.addAttribute("familiar", familiar);
            // Idealmente, un error más específico (ej. "El correo ya está en uso")
            model.addAttribute("error", "Error durante el registro. Por favor, verifique sus datos.");
            return "registro";
        }
    }

    @GetMapping("/")
    public String verPaginaDeInicio() {
        return "redirect:/login";
    }
}