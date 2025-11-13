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
import org.springframework.core.io.ClassPathResource;

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

    // Envío correo reseteo
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

    // Envío correo bienvenido
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

    // Envío masivo - plantilla bulk-email

    @Async
    @Override
    public void sendBulkEmail(List<String> recipients, String subject, String body) {
        if (recipients == null || recipients.isEmpty()) {
            System.err.println("No hay destinatarios para el correo masivo.");
            return;
        }

        try {

            // Convertimos el texto plano del admin (con saltos de línea) en HTML.
            String formattedBody = formatTextToHtml(body);

            // Preparar contexto para Thymeleaf
            Context context = new Context();
            context.setVariable("subject", subject);
            context.setVariable("body", formattedBody); // Usamos el texto ya formateado

            String htmlContent = templateEngine.process("emails/bulk-email", context); // Apunta a tu nueva plantilla

            // Crear MimeMessage y Helper
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // true = multipart (necesario para la imagen del logo)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Configurar destinatarios y asunto
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true para indicar que es HTML
            helper.setFrom(fromEmail);

            // Configurar "Para" (Anti-Spam) y "BCC" (Privacidad)
            helper.setTo(fromEmail); // Envía "Para" nuestra propia cuenta
            helper.setBcc(recipients.toArray(new String[0])); // Todos los demás en copia oculta

            // Adjuntar logo inline
            try {
                ClassPathResource logo = new ClassPathResource("static/images/Geri_Logo-.png");
                if (logo.exists()) {
                    helper.addInline("geriLogo", logo);
                }
            } catch (Exception e) {
                System.err.println("Error adjuntando logo inline: " + e.getMessage());
            }

            // Enviar el correo
            mailSender.send(mimeMessage);

            System.out.println("Correo masivo enviado a " + recipients.size() + " destinatarios.");
        } catch (MessagingException e) {
            System.err.println("Error al enviar correo masivo: " + e.getMessage());
        }
    }

    @Async
    @Override
    public void sendEmailChangeNotification(String newEmail, String userName) {
        try {
            // Crear mensaje y helper
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Configurar destinatario, asunto y remitente
            helper.setTo(newEmail);
            helper.setSubject("Tu correo en Gericare ha sido actualizado");
            helper.setFrom(fromEmail);

            // Preparar el contexto para la plantilla Thymeleaf
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("newEmail", newEmail);
            context.setVariable("appBaseUrl", baseUrl);

            // Procesar nueva plantilla
            String htmlContent = templateEngine.process("emails/email-change-notification", context);
            helper.setText(htmlContent, true); // true = HTML

            try {
                ClassPathResource logo = new ClassPathResource("static/images/Geri_Logo-.png");
                if (logo.exists()) {
                    helper.addInline("geriLogo", logo);
                }
            } catch (Exception e) {
                // Usa System.err para ser consistente con el método sendBulkEmail
                System.err.println("Error adjuntando logo inline en sendEmailChangeNotification: " + e.getMessage());
            }

            // Enviar correo
            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            // Manejo excepción
            System.err.println("Fallo al enviar el correo de notificación de cambio de email: " + e.getMessage());
        }
    }

    // Metodo auxiliar para convertir texto plano con saltos de línea en HTML
    private String formatTextToHtml(String text) {

        // Definimos el estilo base para cada párrafo
        String pStyle = "style=\"font-family: 'Poppins', Arial, sans-serif; font-size: 16px; color: #444444; line-height: 1.7; margin: 0 0 15px 0; text-align: center;\""; // <-- LÍNEA ACTUALIZADA

        if (text == null || text.isBlank()) {
            // Añadimos el estilo centrado también al mensaje por defecto
            return "<p " + pStyle + ">No se proporcionó contenido.</p>";
        }

        // Escapa caracteres HTML básicos para seguridad
        String safeText = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");

        // Reemplazamos los saltos de línea
        String html = safeText
                .trim()
                .replaceAll("\r\n", "\n")
                .replaceAll("\n{2,}", "</p><p " + pStyle + ">") // 2 o más "Enter" = nuevo párrafo
                .replaceAll("\n", "<br>"); // 1 "Enter" = salto de línea <br>

        // Envolvemos todo en el primer parrafo
        return "<p " + pStyle + ">" + html + "</p>";
    }
}
