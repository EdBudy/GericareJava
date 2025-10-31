package com.example.Gericare.Entity;

import com.example.Gericare.Enums.EstadoUsuario; // o EstadoRegistro si lo creaste
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_medicamento")
@Getter
@Setter
@NoArgsConstructor
public class Medicamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medicamento")
    private Long idMedicamento;

    @Column(name = "nombre_medicamento", nullable = false, length = 100)
    private String nombreMedicamento;

    @Column(name = "descripcion_medicamento", nullable = false, length = 250)
    private String descripcionMedicamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoUsuario estado = EstadoUsuario.Activo; // o EstadoRegistro
}