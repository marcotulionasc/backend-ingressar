package br.com.multiprodutora.ticketeria.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class JavaSmtpGmailSenderService {
    @Autowired
    private JavaMailSender emailSender;

    @Value("${MAIL_USERNAME}")
    private String senderEmail;

    private final Logger log = Logger.getLogger(JavaSmtpGmailSenderService.class.getName());

    public void sendEmail(String toEmail, String subject, String body){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        emailSender.send(message);

        log.info("Email enviado para: " + toEmail);
    }
}
