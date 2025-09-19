package com.example.Gericare.entity;

import com.example.Gericare.enums.RolNombre;
import jakarta.persistence.*;

@Entity
@Table(name = "tb_rol")

public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nombre_rol", nullable = false)
    private RolNombre nombre;

    @Column(name = "descripcion_rol")
    private String descripcion;

    // Getters y Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public RolNombre getNombre() {
        return nombre;
    }

    public void setNombre(RolNombre nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
