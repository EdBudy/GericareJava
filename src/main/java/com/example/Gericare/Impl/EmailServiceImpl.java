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

    @Async
    @Override
    public void sendPasswordResetEmail(String to, String token) {
        try {
            String resetUrl = baseUrl + "/reset-password?token=" + token;

            Context context = new Context();
            context.setVariable("resetUrl", resetUrl);

            String htmlContent = templateEngine.process("password-reset-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setText(htmlContent, true);
            helper.setTo(to);
            helper.setSubject("Solicitud de Cambio de Contraseña - Gericare Connect");

            // Establecer quién envía el correo
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Fallo al enviar el correo de reseteo.", e);
        }
    }
}