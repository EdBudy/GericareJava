package com.example.Gericare.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
    private String dosis;
    private String frecuencia;
    private String instrucciones;
}