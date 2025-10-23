package com.example.Gericare.Impl;

import com.example.Gericare.Service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.base-url}")
    private String baseUrl;

    // Inyectar el email 'from' desde application.properties en vez de escribirlo fijo en el código
    @Value("${spring.mail.from}")
    private String fromEmail;

    @Async // Se ejecuta en segundo plano para no demorar al usuario
    @Override
    public void sendPasswordResetEmail(String to, String token) {
        try {
            // Construir URL completa, ej: http://localhost:8080/reset-password?token=...
            String resetUrl = baseUrl + "/reset-password?token=" + token;

            // Crea el “contexto” para Thymeleaf, donde guarda las variables a usar en la plantilla
            Context context = new Context();
            context.setVariable("resetUrl", resetUrl); // Guardar URL de reseteo para usarla en la plantilla

            // Procesar la plantilla HTML del correo con Thymeleaf (cuerpo del correo)
            String htmlContent = templateEngine.process("emails/password-reset-email", context);

            // Crear correo en formato MIME, permite enviar correos en HTML
            // (Multipurpose Internet Mail Extensions) permite adjuntar archivos, imágenes, etc.
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Pone el contenido del correo (HTML) y le indica que es un email HTML
            helper.setText(htmlContent, true);
            // Definir destinatario del correo
            helper.setTo(to);
            // Definir asunto del correo
            helper.setSubject("Solicitud de Cambio de Contraseña - Gericare Connect");

            // Definir quién envía el correo
            helper.setFrom(fromEmail);

            // Configurar y envíar correo usando JavaMailSender
            // (interfaz de Spring Framework que simplifica el envío de correos electrónicos en aplicaciones Java)
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Fallo al enviar el correo de reseteo.", e);
        }
    }

    @Async
    @Override
    public void sendWelcomeEmail(String to, String nombre, String documentoIdentificacion) {
        try {
            Context context = new Context();
            context.setVariable("nombreUsuario", nombre);
            context.setVariable("documentoIdentificacion", documentoIdentificacion);
            context.setVariable("loginUrl", baseUrl + "/login"); // URL inicio de sesión

            // Procesar la nueva plantilla 'welcome-email.html'
            String htmlContent = templateEngine.process("emails/welcome-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setText(htmlContent, true); // Indicar que es HTML
            helper.setTo(to);                  // Destinatario
            helper.setSubject("¡Bienvenido a Gericare Connect!"); // Asunto
            helper.setFrom(fromEmail);         // Remitente (desde application.properties)

            mailSender.send(mimeMessage);      // Enviar
        } catch (MessagingException e) {
            System.err.println("Error enviando email de bienvenida: " + e.getMessage()); // Mejor loggear el error
            throw new IllegalStateException("Fallo al enviar el correo de bienvenida.", e);
        }
    }
}