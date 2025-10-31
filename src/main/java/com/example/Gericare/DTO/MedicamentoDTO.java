package com.example.Gericare.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicamentoDTO {
    private Long idMedicamento;

    @NotBlank(message = "El nombre no puede estar vacío") // Es buena práctica añadirlo aquí también
    private String nombreMedicamento;

    @NotBlank(message = "La descripción no puede estar vacía") // Añadir esta anotación
    private String descripcionMedicamento;
}