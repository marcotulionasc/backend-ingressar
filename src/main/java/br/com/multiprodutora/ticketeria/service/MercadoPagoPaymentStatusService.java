package br.com.multiprodutora.ticketeria.service;

import br.com.multiprodutora.ticketeria.config.RestTemplateConfig;
import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import br.com.multiprodutora.ticketeria.repository.PaymentRepository;
import br.com.multiprodutora.ticketeria.utils.MPAPIPayment;
import br.com.multiprodutora.ticketeria.utils.MPAPISearchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MercadoPagoPaymentStatusService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${mp.token}")
    private String mercadoPagoAccessToken;

    @Autowired
    private RestTemplateConfig restTemplate;

    private static final String MP_SEARCH_URL = "https://api.mercadopago.com/v1/payments/search";

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(PaymentProcessingService.class);

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void updatePaymentStatus() {
        logger.info("Starting payment status update...");

        try {
            List<Payment> pendingPayments = paymentRepository.findByStatus(Status.PENDING);
            logger.info("Found {} pending payments to process.", pendingPayments.size());

            for (Payment localPayment : pendingPayments) {
                try {
                    String externalReference = localPayment.getId();
                    String url = MP_SEARCH_URL + "?external_reference=" + externalReference;
                    logger.info("Checking payment status for Payment ID: {} with URL: {}", externalReference, url);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(mercadoPagoAccessToken);

                    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
                    ResponseEntity<String> response = restTemplate.restTemplate().exchange(url, HttpMethod.GET, requestEntity, String.class);

                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        MPAPISearchResponse mpApiResponse = objectMapper.readValue(response.getBody(), MPAPISearchResponse.class);

                        if (mpApiResponse.getResults() != null && !mpApiResponse.getResults().isEmpty()) {
                            MPAPIPayment mpPayment = mpApiResponse.getResults().get(0);
                            String mpStatus = mpPayment.getStatus();
                            logger.info("Payment ID: {} - Mercado Pago status: {}", externalReference, mpStatus);

                            switch (mpStatus.toLowerCase()) {
                                case "approved":
                                    localPayment.setStatus(Status.APPROVED);
                                    break;
                                case "rejected":
                                    localPayment.setStatus(Status.REJECTED);
                                    break;
                                case "in_process":
                                    localPayment.setStatus(Status.IN_PROCESS);
                                    break;
                                case "cancelled":
                                    localPayment.setStatus(Status.CANCELED);
                                    break;
                                default:
                                    logger.warn("Payment ID: {} - Unknown status: {}", externalReference, mpStatus);
                            }

                            paymentRepository.save(localPayment);
                            logger.info("Payment ID: {} status updated to: {}", externalReference, localPayment.getStatus());

                            // Verificação adicional no banco
                            logger.info("Verifying updated payment in database...");
                            Payment updatedPayment = paymentRepository.findById(localPayment.getId()).orElse(null);
                            if (updatedPayment != null) {
                                logger.info("Payment ID: {}, Status in database: {}", updatedPayment.getId(), updatedPayment.getStatus());
                            } else {
                                logger.warn("Payment ID: {} not found in database after update.", localPayment.getId());
                            }
                        } else {
                            logger.warn("Payment ID: {} - No results returned from Mercado Pago API.", externalReference);
                        }
                    } else {
                        logger.error("Payment ID: {} - Failed to fetch status. HTTP Status: {}", externalReference, response.getStatusCode());
                    }
                } catch (Exception e) {
                    logger.error("Error while processing Payment ID: {} - {}", localPayment.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error in updatePaymentStatus - {}", e.getMessage(), e);
        }

        logger.info("Finished payment status update.");
    }


}
