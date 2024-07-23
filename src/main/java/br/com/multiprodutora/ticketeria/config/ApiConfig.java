package br.com.multiprodutora.ticketeria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {

    @Value("${api.base.url}")
    private String apiBaseUrl;

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
}
