package br.com.multiprodutora.ticketeria.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MPAPISearchResponse {
    private List<MPAPIPayment> results;
    private Paging paging; // Classe que você cria para mapear os campos de paging

    public List<MPAPIPayment> getResults() {
        return results;
    }

    public void setResults(List<MPAPIPayment> results) {
        this.results = results;
    }

    public Paging getPaging() {
        return paging;
    }

    public void setPaging(Paging paging) {
        this.paging = paging;
    }
}
