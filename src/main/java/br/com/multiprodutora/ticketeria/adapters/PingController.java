package br.com.multiprodutora.ticketeria.adapters;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/ping")
public class PingController {

        private static final Logger log = LoggerFactory.getLogger(PingController.class);
        private Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        private String getClientIp(HttpServletRequest request) {
                String clientIp = request.getHeader("X-Forwarded-For");
                if (clientIp == null || clientIp.isEmpty()) {
                        clientIp = request.getRemoteAddr();
                } else {
                        clientIp = clientIp.split(",")[0];
                }
                return clientIp;
        }

        @RequestMapping
        public ResponseEntity<String> ping(HttpServletRequest request) {
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
                String formattedDateTime = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                String clientIp = getClientIp(request);
                log.info("{} - Checked the Ingressar microservice is alive from IP: {}", formattedDateTime, clientIp);
                return ResponseEntity.ok("checked the Ingressar microservice is alive!");
        }
}