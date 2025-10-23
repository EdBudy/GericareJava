package com.example.Gericare.Entity;

import com.example.Gericare.Enums.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tb_paciente")
public class Paciente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Long idPaciente;

    @Column(name = "documento_identificacion", nullable = false, unique = true)
    private String documentoIdentificacion;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genero genero;

    @Column(name = "contacto_emergencia", nullable = false, length = 20)
    private String contactoEmergencia;

    @Column(name = "estado_civil", nullable = false, length = 30)
    private String estadoCivil;

    @Convert(converter = TipoSangreConverter.class)
    @Column(name = "tipo_sangre", nullable = false)
    private TipoSangre tipoSangre;

    @Column(name = "seguro_medico", length = 100)
    private String seguroMedico;

    @Column(name = "numero_seguro", length = 50)
    private String numeroSeguro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPaciente estado;

}