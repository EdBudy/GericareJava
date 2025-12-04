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

import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.from:gericareconnect@gmail.com}")
    private String fromEmail;

    // 1. Reseteo de Contraseña
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
            enviarCorreoBase(to, "Solicitud de Cambio de Contraseña - Gericare Connect", htmlContent);

        } catch (MessagingException e) {
            throw new IllegalStateException("Fallo al enviar el correo de reseteo.", e);
        }
    }

    // 2. Bienvenida
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
            enviarCorreoBase(to, "¡Bienvenido a Gericare Connect!", htmlContent);

        } catch (MessagingException e) {
            throw new IllegalStateException("Fallo al enviar el correo de bienvenida.", e);
        }
    }

    // 3. Correo Masivo
    @Async
    @Override
    public void sendBulkEmail(List<String> recipients, String subject, String body) {
        if (recipients == null || recipients.isEmpty()) return;

        try {
            String formattedBody = formatTextToHtml(body);
            Context context = new Context();
            context.setVariable("subject", subject);
            context.setVariable("body", formattedBody);

            String htmlContent = templateEngine.process("emails/bulk-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            helper.setTo(fromEmail); // Se envía a sí mismo para ocultar destinatarios
            helper.setBcc(recipients.toArray(new String[0])); // Destinatarios en copia oculta

            agregarLogosInline(helper);

            mailSender.send(mimeMessage);
            System.out.println("Correo masivo enviado a " + recipients.size() + " destinatarios.");

        } catch (MessagingException e) {
            System.err.println("Error al enviar correo masivo: " + e.getMessage());
        }
    }

    // 4. Notificación Cambio de Correo
    @Async
    @Override
    public void sendEmailChangeNotification(String newEmail, String userName) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("newEmail", newEmail);
            context.setVariable("appBaseUrl", baseUrl);

            String htmlContent = templateEngine.process("emails/email-change-notification", context);
            enviarCorreoBase(newEmail, "Tu correo en Gericare ha sido actualizado", htmlContent);

        } catch (MessagingException e) {
            System.err.println("Fallo al enviar notificación de cambio de email: " + e.getMessage());
        }
    }

    // --- Métodos Auxiliares Privados para evitar código repetido ---

    private void enviarCorreoBase(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.setFrom(fromEmail);

        agregarLogosInline(helper);

        mailSender.send(mimeMessage);
    }

    private void agregarLogosInline(MimeMessageHelper helper) {
        try {
            ClassPathResource logo = new ClassPathResource("static/images/Geri_Logo-..png");
            if (logo.exists()) helper.addInline("geriLogo", logo);
        } catch (Exception e) {

        }
    }



    private String formatTextToHtml(String text) {
        String pStyle = "style=\"font-family: 'Poppins', Arial, sans-serif; font-size: 16px; color: #444444; line-height: 1.7; margin: 0 0 15px 0; text-align: center;\"";
        if (text == null || text.isBlank()) return "<p " + pStyle + ">Sin contenido.</p>";

        String safeText = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        String html = safeText.trim()
                .replaceAll("\r\n", "\n")
                .replaceAll("\n{2,}", "</p><p " + pStyle + ">")
                .replaceAll("\n", "<br>");
        return "<p " + pStyle + ">" + html + "</p>";
    }
}