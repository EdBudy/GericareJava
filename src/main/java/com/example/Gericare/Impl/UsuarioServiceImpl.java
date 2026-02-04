package com.example.Gericare.Impl;

import com.example.Gericare.DTO.*;
import com.example.Gericare.Repository.*;
import com.example.Gericare.Service.EmailService;
import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.Service.PacienteAsignadoService;
import com.example.Gericare.Entity.*;
import com.example.Gericare.Enums.EstadoAsignacion;
import com.example.Gericare.Enums.EstadoUsuario;
import com.example.Gericare.Enums.RolNombre;
import com.example.Gericare.specification.UsuarioSpecification;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private PacienteAsignadoRepository pacienteAsignadoRepository;
    @Autowired @Lazy private PacienteAsignadoService pacienteAsignadoService;
    @Autowired private EmailService emailService;

    // --- MÉTODOS DE RECUPERACIÓN DE CONTRASEÑA ---

    @Override
    public void createPasswordResetTokenForUser(String email) {
        Usuario usuario = usuarioRepository.findByCorreoElectronico(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        String token = UUID.randomUUID().toString();
        usuario.setResetPasswordToken(token);
        usuario.setResetPasswordTokenExpiryDate(LocalDateTime.now().plusHours(1));
        usuarioRepository.save(usuario);

        String resetUrl = "http://localhost:8080/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(usuario.getCorreoElectronico(), resetUrl);
    }

    @Override
    public String validatePasswordResetToken(String token) {
        return usuarioRepository.findByResetPasswordToken(token)
                .filter(u -> u.getResetPasswordTokenExpiryDate().isAfter(LocalDateTime.now()))
                .map(u -> (String) null)
                .orElse("invalidToken");
    }

    @Override
    @Transactional
    public void changeUserPassword(String token, String newPassword) {
        Usuario usuario = usuarioRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        usuario.setContrasena(passwordEncoder.encode(newPassword));
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordTokenExpiryDate(null);
        usuario.setNecesitaCambioContrasena(false); // <--- ESTO EVITA EL BUCLE DE LOGIN
        usuarioRepository.save(usuario);
    }

    // --- EL RESTO DE TU CÓDIGO (CREAR, LISTAR, EXCEL, PDF) ---
    // (Mantén aquí todos tus métodos crearCuidador, crearFamiliar, exportarUsuariosAExcel, etc. tal cual los tienes)
    
    // [Aquí irían tus métodos crearCuidador, listarTodosLosUsuarios, exportarUsuariosAExcel, etc...]
    // Asegúrate de copiar y pegar el cuerpo de esos métodos desde tu archivo original aquí.
}
