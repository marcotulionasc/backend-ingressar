package br.com.multiprodutora.ticketeria.adapters;

import br.com.multiprodutora.ticketeria.service.JavaSmtpGmailSenderWithAttachmentService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private JavaSmtpGmailSenderWithAttachmentService emailSenderService;

    @PostMapping("/send-ticket")
    public ResponseEntity<String> sendTicketEmail(
            @RequestParam("email") String email,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam("file") MultipartFile file) {

        try {
            // Salvar o arquivo temporariamente
            File tempFile = File.createTempFile("ticket", ".pdf");
            file.transferTo(tempFile);

            // Enviar e-mail com o anexo
            emailSenderService.sendEmailWithAttachment(email, subject, body, tempFile);

            // Deletar o arquivo temporário
            tempFile.delete();

            return ResponseEntity.ok("E-mail enviado com sucesso.");
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao enviar e-mail.");
        }
    }
}
