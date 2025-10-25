package com.example.Gericare.Entity;

import com.example.Gericare.Enums.EstadoUsuario; // o EstadoRegistro
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_enfermedad")
@Getter
@Setter
@NoArgsConstructor
public class Enfermedad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_enfermedad")
    private Long idEnfermedad;

    @Column(name = "nombre_enfermedad", nullable = false, length = 100)
    private String nombreEnfermedad;

    @Column(name = "descripcion_enfermedad", length = 250)
    private String descripcionEnfermedad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoUsuario estado = EstadoUsuario.Activo; // o EstadoRegistro
}