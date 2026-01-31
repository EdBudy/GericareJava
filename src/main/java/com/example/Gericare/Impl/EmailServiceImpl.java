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
    private JavaMailSender mailSender; // Componente de envío SMTP

    @Autowired
    private TemplateEngine templateEngine; // Motor de plantillas HTML

    // 🟢 INYECCIÓN INTELIGENTE DE URL
    // Lee 'app.base-url' del properties. Si no existe, usa localhost por defecto.
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.from}")
    private String fromEmail;

    /**
     * Envía el correo de recuperación de contraseña.
     * Construye el link usando la URL base dinámica (Azure o Local).
     */
    @Async
    @Override
    public void sendPasswordResetEmail(String to, String token) {
        try {
            // Construimos la URL completa aquí para asegurar que coincida con el entorno
            String resetUrl = baseUrl + "/reset-password?token=" + token;
            
            Context context = new Context();
            context.setVariable("resetUrl", resetUrl);
            context.setVariable("email", to);
            context.setVariable("appBaseUrl", baseUrl); // Para links al home/login

            String htmlContent = templateEngine.process("emails/password-reset-email", context);
            enviarCorreoBase(to, "Solicitud de Cambio de Contraseña - Gericare Connect", htmlContent);
            
            System.out.println("✅ Correo de recuperación enviado a: " + to);

        } catch (MessagingException e) {
            System.err.println("❌ Error enviando correo de recuperación a " + to + ": " + e.getMessage());
            throw new IllegalStateException("Fallo al enviar el correo de reseteo.", e);
        }
    }

    /**
     * Envía correo de bienvenida con credenciales o instrucciones iniciales.
     */
    @Async // Recomendable hacerlo Async para no bloquear el registro
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
            
            System.out.println("✅ Correo de bienvenida enviado a: " + to);

        } catch (MessagingException e) {
            System.err.println("❌ Error enviando bienvenida a " + to + ": " + e.getMessage());
            // No lanzamos excepción crítica aquí para no revertir la transacción del registro de usuario
        }
    }

    /**
     * Envía correos masivos utilizando Copia Oculta (BCC) para privacidad.
     */
    @Async
    @Override
    public void sendBulkEmail(List<String> recipients, String subject, String body) {
        if (recipients == null || recipients.isEmpty()) return;

        try {
            String formattedBody = formatTextToHtml(body);

            Context context = new Context();
            context.setVariable("subject", subject);
            context.setVariable("body", formattedBody);
            context.setVariable("appBaseUrl", baseUrl);

            String htmlContent = templateEngine.process("emails/bulk-email", context);
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            
            // Truco Pro: Enviamos el correo a nosotros mismos y ponemos a todos en BCC
            helper.setTo(fromEmail); 
            helper.setBcc(recipients.toArray(new String[0]));

            agregarLogosInline(helper);

            mailSender.send(mimeMessage);
            System.out.println("✅ Correo masivo enviado a " + recipients.size() + " destinatarios.");

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar correo masivo: " + e.getMessage());
        }
    }

    /**
     * Notificación de seguridad cuando se cambia el email.
     */
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
            System.err.println("❌ Fallo al enviar notificación de cambio de email: " + e.getMessage());
        }
    }

    // ==========================================
    // MÉTODOS PRIVADOS (HELPER METHODS)
    // ==========================================

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
            // NOTA: Asegúrate de que el archivo se llame EXACTAMENTE así en src/main/resources/static/images/
            // He corregido el nombre para evitar errores con "..png"
            ClassPathResource logo = new ClassPathResource("static/images/Geri_Logo-..png");
            
            if (logo.exists()) {
                // 'geriLogo' es el id que usas en el HTML como th:src="|cid:geriLogo|"
                helper.addInline("geriLogo", logo);
            } else {
                System.out.println("⚠️ Advertencia: No se encontró el logo en la ruta especificada.");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error adjuntando logo inline: " + e.getMessage());
        }
    }

    private String formatTextToHtml(String text) {
        String pStyle = "style=\"font-family: 'Poppins', Arial, sans-serif; font-size: 16px; color: #444444; line-height: 1.7; margin: 0 0 15px 0; text-align: left;\"";

        if (text == null || text.isBlank())
            return "<p " + pStyle + ">Sin contenido.</p>";

        String safeText = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

        String html = safeText.trim()
                .replaceAll("\r\n", "\n")
                .replaceAll("\n{2,}", "</p><p " + pStyle + ">")
                .replaceAll("\n", "<br>");

        return "<p " + pStyle + ">" + html + "</p>";
    }
}
