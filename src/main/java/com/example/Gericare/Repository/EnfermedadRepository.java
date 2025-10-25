package com.example.Gericare.Repository;

import com.example.Gericare.Entity.Enfermedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnfermedadRepository extends JpaRepository<Enfermedad, Long> {
}