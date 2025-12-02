package com.example.Gericare.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import jakarta.validation.constraints.Pattern;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoriaClinicaMedicamentoDTO {
    private Long idHcMedicamento;
    private Long idMedicamento; // Para la selección
    private String nombreMedicamento; // Para mostrar
    @NotBlank(message = "La dosis es obligatoria")
    @Pattern(regexp = "^\\d+(\\.\\d+)?$", message = "La dosis debe ser un número válido (ej: 500 o 2.5)")
    private String dosis;
    private String frecuencia;
    private String instrucciones;
}