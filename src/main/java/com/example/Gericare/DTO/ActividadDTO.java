package com.example.Gericare.DTO;

import com.example.Gericare.enums.EstadoActividad;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor // Mantenemos el constructor sin argumentos
@AllArgsConstructor // Mantenemos el constructor con todos los argumentos
public class ActividadDTO {

    private Long idActividad;

    @NotNull(message = "Debe seleccionar un paciente.")
    private Long idPaciente;

    private String nombrePaciente;
    private Long idAdmin;
    private String nombreAdmin;

    @NotBlank(message = "El tipo de actividad no puede estar vacío.")
    private String tipoActividad;

    @NotBlank(message = "La descripción no puede estar vacía.")
    private String descripcionActividad;

    @NotNull(message = "La fecha no puede estar vacía.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
    private LocalDate fechaActividad;

    @NotNull(message = "La hora de inicio no puede estar vacía.")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin no puede estar vacía.")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaFin;

    private EstadoActividad estadoActividad;

    /**
     * Constructor personalizado para la consulta JPQL del repositorio.
     * Este constructor es invocado por ActividadRepository para mapear eficientemente
     * los resultados de la base de datos a este DTO.
     */
    public ActividadDTO(Long idActividad, String tipoActividad, String descripcionActividad,
                        LocalDate fechaActividad, LocalTime horaInicio, LocalTime horaFin,
                        String nombrePaciente, String apellidoPaciente) {
        this.idActividad = idActividad;
        this.tipoActividad = tipoActividad;
        this.descripcionActividad = descripcionActividad;
        this.fechaActividad = fechaActividad;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.nombrePaciente = nombrePaciente + " " + apellidoPaciente; // Concatenamos el nombre aquí
    }
}

