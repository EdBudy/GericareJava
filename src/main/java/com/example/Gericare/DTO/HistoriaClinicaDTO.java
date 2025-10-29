package com.example.Gericare.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoriaClinicaDTO {
    private Long idHistoriaClinica;
    private Long idPaciente;
    private String nombrePacienteCompleto; // Para mostrar
    private Long idAdministrador; // Quién la creó/última modificó
    private String nombreAdministrador; // Para mostrar

    private String estadoSalud;
    private String condiciones;
    private String antecedentesMedicos;
    private String alergias;
    private String dietasEspeciales;
    private LocalDate fechaUltimaConsulta;
    private String observaciones;
    private boolean completada;

    // Listas para los detalles
    private List<HistoriaClinicaCirugiaDTO> cirugias = new ArrayList<>();
    private List<HistoriaClinicaMedicamentoDTO> medicamentos = new ArrayList<>();
    private List<HistoriaClinicaEnfermedadDTO> enfermedades = new ArrayList<>();
}