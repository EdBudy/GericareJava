package com.example.Gericare.DTO;

import com.example.Gericare.Enums.EstadoActividad; // Reutilizado
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;


import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TratamientoDTO {
    private Long idTratamiento;

    @NotNull(message = "Debe seleccionar un paciente.")
    private Long pacienteId;
    private String pacienteNombreCompleto;

    private Long administradorId;
    private String administradorNombreCompleto;

    private Long cuidadorId;
    private String cuidadorNombreCompleto;

    @NotBlank(message = "La descripción no puede estar vacía.")
    private String descripcion;

    private String instruccionesEspeciales;

    @NotNull(message = "La fecha de inicio no puede estar vacía.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "La fecha de fin no puede ser en el pasado.")
    private LocalDate fechaFin; // Nullable

    private String observaciones; // Nullable

    private EstadoActividad estadoTratamiento;

    // Constructor útil para listar (simplifica joins en JPQL si es necesario)
    public TratamientoDTO(Long idTratamiento, Long pacienteId, String pacienteNombreCompleto, Long cuidadorId, String cuidadorNombreCompleto, String descripcion, LocalDate fechaInicio, LocalDate fechaFin, EstadoActividad estadoTratamiento, String observaciones) {
        this.idTratamiento = idTratamiento;
        this.pacienteId = pacienteId;
        this.pacienteNombreCompleto = pacienteNombreCompleto;
        this.cuidadorId = cuidadorId;
        this.cuidadorNombreCompleto = cuidadorNombreCompleto;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estadoTratamiento = estadoTratamiento;
        this.observaciones = observaciones;
    }
}