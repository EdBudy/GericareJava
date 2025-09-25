package com.example.Gericare.Controller;

import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.entity.Cuidador;
import com.example.Gericare.entity.Familiar;
import com.example.Gericare.entity.Administrador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // Para respuestas HTTP
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios") // Todas las URLs aquí empiezan con "/usuarios"
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // Obtener datos (GET)

    // Obtener la lista de todos los usuarios
    // GET http://localhost:8080/usuarios

    @GetMapping("/lista")
    public String listarUsuarios(Model model) {
        List<UsuarioDTO> usuarios = usuarioService.listarTodosLosUsuarios();
        model.addAttribute("usuarios", usuarios);

        return "admin/usuarios/gestionUsuarios";
    }

    // Obtener un usuario específico por su ID
    // GET http://localhost:8080/usuarios/1
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtenerUsuarioPorId(@PathVariable Long id) {
        // ResponseEntity nos permite devolver un código 404 si no se encuentra.
        return usuarioService.obtenerUsuarioPorId(id)
                .map(usuario -> ResponseEntity.ok(usuario)) // Si lo encuentra, devuelve 200 OK con el usuario.
                .orElse(ResponseEntity.notFound().build()); // Si no, devuelve 404 Not Found.
    }

    // Crear datos (POST)

    // Crear un nuevo cuidador
    // POST http://localhost:8080/usuarios/cuidador
    @PostMapping("/cuidador")
    public UsuarioDTO crearCuidador(@RequestBody Cuidador cuidador) {
        return usuarioService.crearCuidador(cuidador);
    }

    // Crear un nuevo familiar
    // POST http://localhost:8080/usuarios/familiar
    @PostMapping("/familiar")
    public UsuarioDTO crearFamiliar(@RequestBody Familiar familiar) {
        return usuarioService.crearFamiliar(familiar);
    }

    // Crear un nuevo administrador.
    // POST http://localhost:8080/usuarios/administrador
    @PostMapping("/administrador")
    public UsuarioDTO crearAdministrador(@RequestBody Administrador administrador) {
        return usuarioService.crearAdministrador(administrador);
    }

    // Actualizar datos (PUT)

    // Actualizar un usuario existente
    // PUT http://localhost:8080/usuarios/1
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioDTO usuarioDTO) {
        return usuarioService.actualizarUsuario(id, usuarioDTO)
                .map(usuarioActualizado -> ResponseEntity.ok(usuarioActualizado))
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar datos (DELETE)

    // Desactivar un usuario (borrado lógico)
    // DELETE http://localhost:8080/usuarios/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        // Verificar si el usuario existe.
        if (usuarioService.obtenerUsuarioPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build(); // Si no existe, 404 Not Found.
        }
        // Si existe, se elimina (desactivar)
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build(); // Devolver 204 No Content, osea: "todo OK, pero no hay nada que mostrar".
    }
}