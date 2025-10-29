package com.example.Gericare.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicamentoDTO {
    private Long idMedicamento;
    private String nombreMedicamento;
    private String descripcionMedicamento;
}