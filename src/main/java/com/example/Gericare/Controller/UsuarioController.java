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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/usuarios") // Todas las URLs aquí empiezan con "/usuarios"
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
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
    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return "redirect:/dashboard";
    }

    // Eliminar datos (DELETE)

    // Desactivar un usuario (borrado lógico)
    // DELETE http://localhost:8080/usuarios/1
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuarioPorGet(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id); // Este método ya hace el borrado lógico
        return "redirect:/dashboard?delete=success";
    }

    //Método para el implement de Excel
    @GetMapping("/exportExcel")
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

    //Metodo para el implement de PDF
    @GetMapping("/exportPdf")
    public ResponseEntity<InputStreamResource> exportarPdf() throws IOException {
        //Creamos el flujo de salida en memoria (Array de bytes)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //Llamamos al servicio para que escriba en el flujo de salida
        usuarioServiceImpl.exportarUsuariosAPDF(outputStream);
        // Configuramos las cabeceras HTTP de la respuesta
        HttpHeaders headers = new HttpHeaders();
        //Definimos el tipo de contenido para el archivo PDF
        headers.setContentType(MediaType.APPLICATION_PDF);
        //Indicamos al navegador que debe tratar la respuesta como un archivo adjunto para su descarga
        headers.setContentDispositionFormData("attachment", "users.pdf");
        //Creamos y devolvemos la respuesta HTTP
        //Creamos un InputStream a partir del array de bytes
        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray())), headers, HttpStatus.OK);
    }
}