package br.com.multiprodutora.ticketeria.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MPAPIPayment {
    private Long id;
    private String status;


}
