package com.example.Gericare.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "telefonos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Telefono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTelefono;

    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @ToString.Exclude // Excluir para evitar bucles
    @EqualsAndHashCode.Exclude // Excluir para evitar bucles
    private Usuario usuario;
}