package br.com.multiprodutora.ticketeria.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class JavaSmtpGmailSenderWithAttachmentService {

    @Autowired
    private JavaMailSender emailSender;

    @Value("${MAIL_USERNAME}")
    private String senderEmail;

    public void sendEmailWithAttachment(String toEmail, String subject, String body, File attachment)
            throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(senderEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body);

        if (attachment != null && attachment.exists()) {
            helper.addAttachment(attachment.getName(), attachment);
        }

        emailSender.send(message);
        System.out.println("E-mail enviado com sucesso com o anexo.");
    }
}
