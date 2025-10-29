package com.example.Gericare.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnfermedadDTO {
    private Long idEnfermedad;
    private String nombreEnfermedad;
    private String descripcionEnfermedad;
}