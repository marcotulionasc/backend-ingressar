package br.com.multiprodutora.ticketeria.service;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import br.com.multiprodutora.ticketeria.domain.model.payment.dto.TicketDTO;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import br.com.multiprodutora.ticketeria.repository.EventRepository;
import br.com.multiprodutora.ticketeria.repository.PaymentRepository;
import br.com.multiprodutora.ticketeria.repository.TicketRepository;
import br.com.multiprodutora.ticketeria.utils.QRCodeGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
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
import java.io.InputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Service
public class PaymentProcessingService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JavaSmtpGmailSenderWithAttachmentService emailService;

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(PaymentProcessingService.class);

    @Scheduled(fixedDelay = 60000) //:TODO - Change to 30 seconds in the future
    @Transactional
    public void processApprovedPayments() {

        try {

            List<Payment> approvedPayments = paymentRepository.findByStatusAndIsTicketsSent(Status.APPROVED, false);

            for (Payment payment : approvedPayments) {
                try {
                    logger.info("Generating tickets for Payment ID: {}", payment.getId());
                    List<File> pdfTickets = generatedPdfTickets(payment);

                    String email = payment.getUserEmail();
                    String subject = "Seus ingressos chegaram!!";
                    String body = "Olá, " + payment.getUserName() + "!\n\n" +
                            "Seus ingressos para o evento " + "Forró de natal" +
                            " foram aprovados e estão anexados a este e-mail.\n\n" +
                            "Obrigado por comprar conosco e aproveite o evento!";

                    logger.info("Sending email with tickets to: {}", email);
                    emailService.sendEmailWithAttachments(email, subject, body, pdfTickets.toArray(new File[0]));

                    for (File pdfFile : pdfTickets) {
                        pdfFile.delete();
                    }

                    payment.setIsTicketsSent(true); //:TODO - Change to true in the future
                    paymentRepository.save(payment);

                } catch (Exception e) {
                    logger.error("Error while processing approved Payment ID: {} - {}", payment.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error in processApprovedPayments - {}", e.getMessage(), e);
        }
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

        Event eventByTicketId = (Event) eventRepository.findByTicketsId(ticket.getId());
        if (eventByTicketId == null) {
            throw new IllegalStateException("Evento não encontrado para o ticket ID: " + ticket.getId());
        }

        Map<String, String> data = new HashMap<>();
        data.put("nomeEvento", eventByTicketId.getTitleEvent());
        data.put("dataEvento", eventByTicketId.getDate() != null ? eventByTicketId.getDate() : "Data não disponível");
        data.put("localEvento", eventByTicketId.getLocal() + " - " + eventByTicketId.getAddress());
        data.put("aberturaCasa", eventByTicketId.getHourOfStart());
        data.put("nomeIngresso", ticket.getNameTicket());
        data.put("areaIngresso", ticket.getAreaTicket());
        data.put("valorLote", String.valueOf(payment.getTotalAmount()));
        data.put("taxaLote", String.valueOf(ticket.getLot().getTaxPriceTicket()));
        data.put("dataCompra", payment.getCreatedAt().toString());
        data.put("nomeComprador", payment.getUserName());
        data.put("idIngresso", String.valueOf(ticket.getId()));

        return data;
    }

    private File generatePdfForTicket(Map<String, String> ticketData, String userEmail) throws IOException, WriterException {

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);

        float margin = 50;
        float yStart = page.getMediaBox().getHeight() - margin;

        PDFont titleFont = PDType1Font.HELVETICA_BOLD;
        PDFont headingFont = PDType1Font.HELVETICA_BOLD;
        PDFont regularFont = PDType1Font.HELVETICA;

        float titleFontSize = 18;
        float headingFontSize = 14;
        float regularFontSize = 12;

        InputStream logoStream = getClass().getResourceAsStream("/logo.png");
        PDImageXObject logoImage = null;
        if (logoStream != null) {
            logoImage = PDImageXObject.createFromByteArray(document, logoStream.readAllBytes(), "logo");
        }

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        if (logoImage != null) {
            float logoWidth = 100;
            float logoHeight = 100;

            float pageWidth = page.getMediaBox().getWidth();
            float logoX = (pageWidth - logoWidth) / 2;
            float logoY = yStart - logoHeight;
            contentStream.drawImage(logoImage, logoX, logoY, logoWidth, logoHeight);
            yStart = logoY - 20;
        }

        contentStream.beginText();
        contentStream.setFont(titleFont, titleFontSize);

        float pageWidth = page.getMediaBox().getWidth();
        String eventTitle = "Ingresso para: " + ticketData.get("nomeEvento");
        float titleWidth = titleFont.getStringWidth(eventTitle) / 1000 * titleFontSize;
        float titleX = (pageWidth - titleWidth) / 2;
        contentStream.newLineAtOffset(titleX, yStart);
        contentStream.showText(eventTitle);
        contentStream.endText();

        yStart -= (titleFontSize + 20);

        BiConsumer<String, Float> writeHeading = (text, currentY) -> {
            try {
                contentStream.beginText();
                contentStream.setFont(headingFont, headingFontSize);
                contentStream.newLineAtOffset(margin, currentY);
                contentStream.showText(text);
                contentStream.endText();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        BiConsumer<String, Float> writeText = (text, currentY) -> {
            try {
                contentStream.beginText();
                contentStream.setFont(regularFont, regularFontSize);
                contentStream.newLineAtOffset(margin, currentY);
                contentStream.showText(text);
                contentStream.endText();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Consumer<Float> drawLine = (currentY) -> {
            try {
                contentStream.setStrokingColor(0, 0, 0);
                contentStream.moveTo(margin, currentY);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, currentY);
                contentStream.stroke();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        yStart -= 10;
        drawLine.accept(yStart);
        yStart -= 20;

        writeHeading.accept("Detalhes do Evento", yStart);
        yStart -= (headingFontSize + 10);

        writeText.accept("Data: " + ticketData.get("dataEvento"), yStart);
        yStart -= (regularFontSize + 5);
        writeText.accept("Local: " + ticketData.get("localEvento"), yStart);
        yStart -= (regularFontSize + 5);
        writeText.accept("Abertura Casa: " + ticketData.get("aberturaCasa"), yStart); // Corrigi o texto para "Abertura Casa"
        yStart -= (regularFontSize + 5);

        yStart -= 10;
        drawLine.accept(yStart);
        yStart -= 20;

        writeHeading.accept("Detalhes do Ingresso", yStart);
        yStart -= (headingFontSize + 10);

        writeText.accept("Ingresso: " + ticketData.get("nomeIngresso"), yStart);
        yStart -= (regularFontSize + 5);
        writeText.accept("Área: " + ticketData.get("areaIngresso"), yStart);
        yStart -= (regularFontSize + 5);
        writeText.accept("Valor: R$" + ticketData.get("valorLote") + " + Taxa: %" + ticketData.get("taxaLote"), yStart);
        yStart -= (regularFontSize + 5);

        yStart -= 10;
        drawLine.accept(yStart);
        yStart -= 20;

        writeHeading.accept("Detalhes da Compra", yStart);
        yStart -= (headingFontSize + 10);

        writeText.accept("Comprado por: " + ticketData.get("nomeComprador"), yStart);
        yStart -= (regularFontSize + 5);
        writeText.accept("Data da compra: " + ticketData.get("dataCompra"), yStart);
        yStart -= (regularFontSize + 5);

        yStart -= 30;

        String ticketId = ticketData.get("idIngresso");
        String qrEndpoint = "https://backend-ingressar.onrender.com/updateStatus?ticketId=" + ticketId + "&status=AUTHORIZED";
        ByteArrayOutputStream qrCodeStream = QRCodeGenerator.generateQRCodeImage(qrEndpoint, 150, 150);
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, qrCodeStream.toByteArray(), "qrcode");

        float qrWidth = 150;
        float qrHeight = 150;
        float qrX = (pageWidth - qrWidth) / 2;
        float qrY = yStart - qrHeight;
        contentStream.drawImage(pdImage, qrX, qrY, qrWidth, qrHeight);

        yStart = qrY - 20;

        contentStream.close();

        File tempFile = File.createTempFile("ticket_" + ticketId, ".pdf");
        document.save(tempFile);
        document.close();

        return tempFile;
    }

}
