package br.com.multiprodutora.ticketeria.service;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import br.com.multiprodutora.ticketeria.domain.model.payment.dto.TicketDTO;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import br.com.multiprodutora.ticketeria.repository.PaymentRepository;
import br.com.multiprodutora.ticketeria.repository.TicketRepository;
import br.com.multiprodutora.ticketeria.utils.QRCodeGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import jakarta.mail.MessagingException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class PaymentProcessingService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private JavaSmtpGmailSenderWithAttachmentService emailService;

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(PaymentProcessingService.class);

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processApprovedPayments() {
        logger.info("Starting approved payments processing...");

        try {
            // Buscar apenas pagamentos aprovados e que ainda não tiveram tickets enviados
            List<Payment> approvedPayments = paymentRepository.findByStatusAndIsTicketsSent(Status.APPROVED, false);
            logger.info("Found {} approved payments to process.", approvedPayments.size());

            for (Payment payment : approvedPayments) {
                try {
                    logger.info("Generating tickets for Payment ID: {}", payment.getId());
                    List<File> pdfTickets = generatedPdfTickets(payment);

                    String email = payment.getUserEmail();
                    String subject = "Seus ingressos chegaram!!";
                    String body = "Olá, " + payment.getUserName() + "!\n\n" +
                            "Seus ingressos para o evento " + payment.getEvent().getNameEvent() +
                            " foram aprovados e estão anexados a este e-mail.\n\n" +
                            "Obrigado por comprar conosco e aproveite o evento!";

                    logger.info("Sending email with tickets to: {}", email);
                    // Passa os anexos como array
                    emailService.sendEmailWithAttachments(email, subject, body, pdfTickets.toArray(new File[0]));

                    for (File pdfFile : pdfTickets) {
                        if (pdfFile.delete()) {
                            logger.info("Temporary ticket file deleted: {}", pdfFile.getName());
                        } else {
                            logger.warn("Failed to delete temporary ticket file: {}", pdfFile.getName());
                        }
                    }

                    payment.setIsTicketsSent(true);
                    paymentRepository.save(payment);
                    logger.info("Payment ID: {} - Tickets sent and status updated.", payment.getId());

                } catch (Exception e) {
                    logger.error("Error while processing approved Payment ID: {} - {}", payment.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error in processApprovedPayments - {}", e.getMessage(), e);
        }

        logger.info("Finished approved payments processing.");
    }

    private List<File> generatedPdfTickets(Payment payment) throws IOException, WriterException {

        List<File> pdfFiles = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        List<TicketDTO> selectedTickets = objectMapper.readValue(payment.getSelectedTicketsJson(),
                new TypeReference<List<TicketDTO>>() {});

        for (TicketDTO selectedTicket : selectedTickets) {

            for (int i = 0; i < selectedTicket.getQuantity(); i++) {

                Optional<Ticket> ticketOpt = ticketRepository.findById(Long.valueOf(selectedTicket.getTicketId()));
                if (ticketOpt.isPresent()) {
                    Ticket ticket = ticketOpt.get();

                    Map<String, String> ticketData = getTicketData(ticket, payment);

                    File pdfFile = generatePdfForTicket(ticketData, payment.getUserEmail());

                    pdfFiles.add(pdfFile);
                }
            }
        }
        return pdfFiles;
    }

    private Map<String, String> getTicketData(Ticket ticket, Payment payment) {
        Map<String, String> data = new HashMap<>();
        data.put("nomeEvento", payment.getEvent().getNameEvent());
        data.put("dataEvento", payment.getEvent().getDate());
        data.put("localEvento", payment.getEvent().getLocal());
        data.put("nomeIngresso", ticket.getNameTicket());
        data.put("areaIngresso", ticket.getAreaTicket());
        data.put("valorLote", String.valueOf(ticket.getLot().getPriceTicket()));
        data.put("taxaLote", String.valueOf(ticket.getLot().getTaxPriceTicket()));
        data.put("dataCompra", payment.getCreatedAt().toString());
        data.put("nomeComprador", payment.getUserName());
        data.put("textoNoIngresso", payment.getEvent().getConfigEvent().getTextThatAppearsOnTheTicket());
        data.put("idIngresso", String.valueOf(ticket.getId()));
        return data;
    }

    private File generatePdfForTicket(Map<String, String> ticketData, String userEmail) throws IOException, WriterException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.setLeading(14.5f);
        contentStream.newLineAtOffset(50, 700);

        contentStream.showText("Evento: " + ticketData.get("nomeEvento"));
        contentStream.newLine();
        contentStream.showText("Data: " + ticketData.get("dataEvento"));
        contentStream.newLine();
        contentStream.showText("Local: " + ticketData.get("localEvento"));
        contentStream.newLine();
        contentStream.showText("Ingresso: " + ticketData.get("nomeIngresso"));
        contentStream.newLine();
        contentStream.showText("Área: " + ticketData.get("areaIngresso"));
        contentStream.newLine();
        contentStream.showText("Valor: R$" + ticketData.get("valorLote") + " + Taxa: R$" + ticketData.get("taxaLote"));
        contentStream.newLine();
        contentStream.showText("Comprado por: " + ticketData.get("nomeComprador"));
        contentStream.newLine();
        contentStream.showText("Data da compra: " + ticketData.get("dataCompra"));
        contentStream.newLine();
        contentStream.showText(ticketData.get("textoNoIngresso"));
        contentStream.newLine();

        contentStream.endText();

        String qrCodeData = "{\"ticketId\":\"" + ticketData.get("idIngresso") + "\",\"userEmail\":\"" + userEmail + "\"}";

        ByteArrayOutputStream qrCodeStream = QRCodeGenerator.generateQRCodeImage(qrCodeData, 100, 100);
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, qrCodeStream.toByteArray(), "qrcode");

        contentStream.drawImage(pdImage, 50, 500, 100, 100);

        contentStream.close();

        File tempFile = File.createTempFile("ticket_" + ticketData.get("idIngresso"), ".pdf");
        document.save(tempFile);
        document.close();

        return tempFile;
    }

}
