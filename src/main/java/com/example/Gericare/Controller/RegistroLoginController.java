package com.example.Gericare.controller;

import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping // Este controlador manejará rutas como /login, /registro
public class RegistroLoginController {

    private final UsuarioService usuarioService;

    public RegistroLoginController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // --- LOGIN ---

    @GetMapping("/login")
    public String mostrarFormularioDeLogin() {
        // Simplemente retorna el nombre del archivo HTML que mostraremos
        return "login";
    }

    // --- REGISTRO ---

    @GetMapping("/registro")
    public String mostrarFormularioDeRegistro(Model model) {
        // Creamos un objeto DTO vacío para que el formulario lo pueda llenar
        model.addAttribute("usuario", new UsuarioDTO());
        // Retornamos el nombre del archivo HTML del formulario de registro
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarCuentaDeUsuario(@ModelAttribute("usuario") UsuarioDTO registroDTO) {
        // Usamos el servicio para guardar el nuevo usuario
        usuarioService.guardar(registroDTO);
        // Redirigimos al usuario a una página de éxito
        return "redirect:/registro?exito";
    }

    // --- PÁGINA DE INICIO ---

    @GetMapping("/")
    public String verPaginaDeInicio() {
        // Esta será la página principal a la que se accede después del login
        return "index";
    }
}