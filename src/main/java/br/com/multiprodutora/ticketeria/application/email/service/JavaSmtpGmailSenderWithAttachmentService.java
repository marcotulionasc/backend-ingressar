package br.com.multiprodutora.ticketeria.application.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
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

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JavaSmtpGmailSenderWithAttachmentService.class);

    /**
     * Envia um e-mail com múltiplos anexos.
     *
     * @param toEmail     Destinatário do e-mail.
     * @param subject     Assunto do e-mail.
     * @param body        Corpo do e-mail.
     * @param attachments Array de arquivos para anexar.
     * @throws MessagingException Se ocorrer um erro no envio do e-mail.
     */
    public void sendEmailWithAttachments(String toEmail, String subject, String body, File... attachments)
            throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true); // 'true' indica multipart

        helper.setFrom(senderEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body);

        if (attachments != null && attachments.length > 0) {
            for (File attachment : attachments) {
                if (attachment.exists()) {
                    helper.addAttachment(attachment.getName(), attachment);
                }
            }
        }

        emailSender.send(message);
        logger.info("E-mail enviado com sucesso com os anexos.");
    }
}

