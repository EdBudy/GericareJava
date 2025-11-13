package com.example.Gericare.Service;

import java.util.List; // Necesaria para el nuevo método de masivos

public interface EmailService {
    void sendPasswordResetEmail(String to, String token);
    void sendWelcomeEmail(String to, String nombre, String documentoIdentificacion);
    void sendBulkEmail(List<String> recipients, String subject, String body);
    //Envía un correo cuando un usuario cambia su correo
    void sendEmailChangeNotification(String newEmail, String userName);
}
 