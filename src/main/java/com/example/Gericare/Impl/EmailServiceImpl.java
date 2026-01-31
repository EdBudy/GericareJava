package com.example.Gericare.Impl;

import com.example.Gericare.Service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    // Logger profesional para monitoreo en Azure
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    // 🟢 URL DINÁMICA:
    // Lee 'app.base-url' (Azure). Si no existe, usa localhost (Tu PC).
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.from:connectgericare@gmail.com}")
    private String fromEmail;

    /**
     * Envía el correo de recuperación de contraseña.
     */
    @Async
    @Override
    public void sendPasswordResetEmail(String to, String token) {
        try {
            // Construimos la URL completa dinámica
            String resetUrl = baseUrl + "/reset-password?token=" + token;
            
            Context context = new Context();
            context.setVariable("resetUrl", resetUrl);
            context.setVariable("email", to);
            context.setVariable("appBaseUrl", baseUrl);

            // Procesamos la plantilla HTML
            String htmlContent = templateEngine.process("emails/password-reset-email", context);
            
            // Enviamos
            enviarCorreoBase(to, "Restablecer Contraseña - Gericare Connect", htmlContent);
            logger.info("✅ Correo de recuperación enviado exitosamente a: {}", to);

        } catch (Exception e) {
            logger.error("❌ Error CRÍTICO enviando recuperación a {}: {}", to, e.getMessage());
        }
    }

    /**
     * Envía correo de bienvenida.
     */
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
            
            logger.info("✅ Correo de bienvenida enviado a: {}", to);

        } catch (Exception e) {
            logger.error("❌ Error enviando bienvenida a {}: {}", to, e.getMessage());
        }
    }

    /**
     * Envía correos masivos (Bulk).
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
            
            // Truco: Enviamos a nosotros mismos y ponemos a los destinatarios en BCC (Privacidad)
            helper.setTo(fromEmail); 
            helper.setBcc(recipients.toArray(new String[0]));

            agregarLogosInline(helper); // Intenta poner logo, si falla no importa

            mailSender.send(mimeMessage);
            logger.info("✅ Correo masivo enviado a {} destinatarios.", recipients.size());

        } catch (Exception e) {
            logger.error("❌ Error enviando correo masivo: {}", e.getMessage());
        }
    }

    /**
     * Notificación de cambio de correo.
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
            enviarCorreoBase(newEmail, "Aviso de Seguridad: Correo Actualizado", htmlContent);

        } catch (Exception e) {
            logger.error("❌ Error enviando notificación de cambio de email: {}", e.getMessage());
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

        // Intentamos adjuntar el logo, pero si falla, el correo sale igual
        agregarLogosInline(helper);

        mailSender.send(mimeMessage);
    }

    private void agregarLogosInline(MimeMessageHelper helper) {
        try {
            // RUTA DEL LOGO: Asegúrate que coincida EXACTAMENTE con tu archivo en resources
            ClassPathResource logo = new ClassPathResource("static/images/Geri_Logo-..png");
            
            if (logo.exists()) {
                helper.addInline("geriLogo", logo);
            } else {
                logger.warn("⚠️ ADVERTENCIA: No se encontró el logo en 'static/images/Geri_Logo-..png'. El correo se enviará sin imagen.");
            }
        } catch (Exception e) {
            // Si hay error con el logo, solo lo registramos y dejamos que el correo continúe
            logger.warn("⚠️ No se pudo adjuntar el logo (Error no crítico): {}", e.getMessage());
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
