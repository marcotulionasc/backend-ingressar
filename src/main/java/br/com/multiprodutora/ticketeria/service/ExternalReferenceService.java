package br.com.multiprodutora.ticketeria.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class ExternalReferenceService {

    @Value("${mp.token}")
    private String mercadoPagoAcessToken;

    public String checkPaymentStatus(String externalReference) throws IOException, UnsupportedEncodingException {
        String accessToken = mercadoPagoAcessToken;
        String encodedExternalReference = URLEncoder.encode(externalReference, "UTF-8");
        String searchUrl = "https://api.mercadopago.com/v1/payments/search?external_reference="
                + encodedExternalReference;

        URL url = new URL(searchUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/json");

        int responseCode = conn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStreamReader reader = new InputStreamReader(conn.getInputStream(), "UTF-8");
            JsonObject responseJson = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray results = responseJson.getAsJsonArray("results");

            if (results == null || results.size() == 0) {
                return "not_found";
            }

            for (JsonElement element : results) {
                JsonObject payment = element.getAsJsonObject();
                String status = payment.get("status").getAsString();
                if ("approved".equalsIgnoreCase(status)) {
                    return "approved";
                }
            }

            return "pending";
        } else {
            InputStreamReader reader = new InputStreamReader(conn.getErrorStream(), "UTF-8");
            JsonObject errorResponse = JsonParser.parseReader(reader).getAsJsonObject();
            throw new IllegalStateException("Erro ao consultar pagamentos. Status HTTP: " + responseCode + ", Erro: "
                    + errorResponse.toString());
        }
    }
}
