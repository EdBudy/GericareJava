package com.example.Gericare.Controller;

import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Impl.UsuarioServiceImpl;
import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.entity.Cuidador;
import com.example.Gericare.entity.Familiar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity; // Para respuestas HTTP
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/usuarios") // Todas las URLs aquí empiezan con "/usuarios"
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;
    private UsuarioServiceImpl usuarioServiceImpl;

    // Obtener datos (GET)

    // Obtener la lista de todos los usuarios
    // GET http://localhost:8080/usuarios
    @GetMapping
    public List<UsuarioDTO> listarTodosLosUsuarios() {
        return usuarioService.listarTodosLosUsuarios();
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
        return ResponseEntity.noContent().build(); // Devolver 204 No Content, osea: "todo OK, pero no hay nada que
        // mostrar".
    }

    //Método para el implement de Excel
    @GetMapping("/users/exportExcel")
    public ResponseEntity<InputStreamResource> exportarExcel() throws IOException {
        //Creamos el flujo de salida en memoria (Array de bytes)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        //Llamamos al servicio para que escriba en el flujo de salida
        usuarioServiceImpl.exportarUsuariosAExcel(outputStream);

        // Configuramos las cabeceras HTTP de la respuesta
        HttpHeaders headers = new HttpHeaders();

        //Definimos el tipo de contenido para el archivo Excel
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        //Indicamos al navegador que debe tratar la respuesta como un archivo adjunto para su descarga
        headers.setContentDispositionFormData("attachment", "users.xlsx");

        //Creamos y devolvemos la respuesta HTTP
        //Creamos un InputStream a partir del array de bytes
        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray())), headers, HttpStatus.OK);
    }

}