package com.example.Gericare.entity;

import com.example.Gericare.enums.RolNombre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tb_rol")

public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @Enumerated(EnumType.STRING)
    @Column(name = "nombre_rol", nullable = false)
    private RolNombre rolNombre;

    @Column(name = "descripcion_rol")
    private String descripcion;

//    @OneToMany(mappedBy = "rolUsuario")
//    private Set<Usuario> usuarios = new HashSet<>();

}
