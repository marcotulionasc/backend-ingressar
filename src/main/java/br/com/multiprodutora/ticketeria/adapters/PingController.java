package br.com.multiprodutora.ticketeria.adapters;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;

@RestController
@RequestMapping("/ping")
public class PingController {

    private static final Logger log = LoggerFactory.getLogger(PingController.class);
    private final Timestamp timestamp = new Timestamp(System.currentTimeMillis());

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
        return ResponseEntity.ok("Ingressar API is up!");
    }
}