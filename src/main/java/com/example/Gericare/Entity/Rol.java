package com.example.Gericare.Entity;

import com.example.Gericare.Enums.RolNombre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
