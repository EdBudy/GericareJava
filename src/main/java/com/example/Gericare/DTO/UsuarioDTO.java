package com.example.Gericare.DTO;

import com.example.Gericare.entity.Rol;
import com.example.Gericare.enums.EstadoUsuario;
import com.example.Gericare.enums.TipoDocumento;
import lombok.*;
import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {


    private Long idUsuario;

    @NotNull(message = "Debe seleccionar un tipo de documento.")
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "El documento de identificación no puede estar vacío.")
    @Size(min = 5, max = 15, message = "El documento debe tener entre 5 y 15 caracteres.")
    @Pattern(regexp = "[0-9]+", message = "El documento solo debe contener números.")
    private String documentoIdentificacion;

    @NotBlank(message = "El nombre no puede estar vacío.")
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacío.")
    private String apellido;

    @NotBlank(message = "La dirección no puede estar vacía.")
    private String direccion;

    @NotBlank(message = "El correo electrónico no puede estar vacío.")
    @Email(message = "El formato del correo electrónico no es válido.")
    private String correoElectronico;

    @NotBlank(message = "La contraseña no puede estar vacía.")
    private String contrasena;

    private EstadoUsuario estado;

    @NotNull(message = "Debe seleccionar un rol.")
    private Rol rol;

    private List<String> telefonos;

    // Campos empleados

    private LocalDate fechaContratacion;
    private String tipoContrato;

    // La validación se aplica solo si el campo no es nulo o vacío
    @Pattern(regexp = "[0-9]*", message = "El contacto de emergencia solo debe contener números.")
    private String contactoEmergencia;

    private LocalDate fechaNacimiento;

    // Campo familiar
    private String parentesco;
}