package br.com.multiprodutora.ticketeria.application.payment.service;

import br.com.multiprodutora.ticketeria.config.RestTemplateConfig;
import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import br.com.multiprodutora.ticketeria.application.payment.repository.PaymentRepository;
import br.com.multiprodutora.ticketeria.utils.MPAPIPayment;
import br.com.multiprodutora.ticketeria.utils.MPAPISearchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        try {
            List<Payment> pendingPayments = paymentRepository.findByStatus(Status.PENDING);

            for (Payment localPayment : pendingPayments) {
                try {
                    String externalReference = localPayment.getId();
                    String url = MP_SEARCH_URL + "?external_reference=" + externalReference;

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
                                    localPayment.setStatus(Status.UNKNOWN); //:TODO explique melhor: O que acontece se o status não for nenhum dos anteriores? O status do pagamento é mantido como pendente?
                            }

                            paymentRepository.save(localPayment);

                            Payment updatedPayment = paymentRepository.findById(localPayment.getId()).orElse(null);
                            return;
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error while processing Payment ID: {} - {}", localPayment.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error in updatePaymentStatus - {}", e.getMessage(), e);
        }
    }
}
