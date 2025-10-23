package com.example.Gericare.Entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("FAMILIAR")
public class Familiar extends Usuario {

    private String parentesco;
}