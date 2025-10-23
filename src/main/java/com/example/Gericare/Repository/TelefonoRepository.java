package com.example.Gericare.Repository;

import com.example.Gericare.Entity.Telefono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelefonoRepository extends JpaRepository<Telefono, Long> {
}