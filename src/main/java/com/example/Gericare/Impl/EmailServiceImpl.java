package com.example.Gericare.Impl;

import com.example.Gericare.Service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List; // ← importante

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.base-url:http://localhost:8090}")
    private String baseUrl;

    @Value("${spring.mail.from:no-reply@example.com}")
    private String fromEmail;

    // ---------------------------
    // Envío correo reseteo
    // ---------------------------
    @Async
    @Override
    public void sendPasswordResetEmail(String to, String token) {
        try {
            String resetUrl = baseUrl + "/reset-password?token=" + token;
            Context context = new Context();
            context.setVariable("resetUrl", resetUrl);
            context.setVariable("email", to);
            context.setVariable("appBaseUrl", baseUrl);

            String htmlContent = templateEngine.process("emails/password-reset-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Solicitud de Cambio de Contraseña - Gericare Connect");
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            // Inline logo si lo tienes en resources/static/images/
            try {
                ClassPathResource logo = new ClassPathResource("static/images/Geri_Logo-.png");
                if (logo.exists()) helper.addInline("geriLogo", logo);
            } catch (Exception e) {
                // no fatal: continuar sin el logo inline
            }

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Fallo al enviar el correo de reseteo.", e);
        }
    }

    // ---------------------------
    // Envío correo bienvenida
    // ---------------------------
    @Async
    @Override
    public void sendWelcomeEmail(String to, String nombre, String documentoIdentificacion) {
        try {
            Context context = new Context();
            context.setVariable("nombreUsuario", nombre);
            context.setVariable("documentoIdentificacion", documentoIdentificacion);
            context.setVariable("email", to);
            context.setVariable("loginUrl", baseUrl + "/login");
            context.setVariable("appBaseUrl", baseUrl);

            String htmlContent = templateEngine.process("emails/welcome-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("¡Bienvenido a Gericare Connect!");
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            // Agregar logo inline si existe
            try {
                ClassPathResource logo = new ClassPathResource("static/images/Geri_Logo-.png");
                if (logo.exists()) helper.addInline("geriLogo", logo);
                ClassPathResource bg = new ClassPathResource("static/images/indeximg.jpg");
                if (bg.exists()) helper.addInline("backgroundImage", bg);
            } catch (Exception e) {
                // ignorar
            }

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Fallo al enviar el correo de bienvenida.", e);
        }
    }

    // ---------------------------
    // Envío masivo - usa plantilla bulk-email
    // ---------------------------
    @Async
    @Override
    public void sendBulkEmail(List<String> recipients, String subject, String body) {
        if (recipients == null || recipients.isEmpty()) {
            System.err.println("No hay destinatarios para el correo masivo.");
            return;
        }

        try {
            
            // Preparar contexto para Thymeleaf (plantilla con subject y body)
            Context context = new Context();
            context.setVariable("subject", subject);
            // Si el body contiene HTML, usar th:utext en la plantilla para interpretar HTML
            context.setVariable("body", body);

            String htmlContent = templateEngine.process("emails/bulk-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // multipart true para permitir inline images
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            helper.setBcc(recipients.toArray(new String[0])); // privacidad: BCC

            // Adjuntar logo inline (opcional)
            try {
                
                ClassPathResource logo = new ClassPathResource("static/images/Geri_Logo-.png");
                if (logo.exists()) helper.addInline("geriLogo", logo);
            } catch (Exception e) { /* ignorar */ }

            mailSender.send(mimeMessage);
ClassPathResource logo = new ClassPathResource("static/images/Geri_Logo-.png");
if (logo.exists()) helper.addInline("geriLogo", logo);

ClassPathResource bg = new ClassPathResource("static/images/indeximg.jpg");
if (bg.exists()) helper.addInline("backgroundImage", bg);

            System.out.println("Correo masivo enviado a " + recipients.size() + " destinatarios.");
        } catch (MessagingException e) {
            System.err.println("Error al enviar correo masivo: " + e.getMessage());
            // Opcional: rethrow o log más elaborado
        }
    }
}
