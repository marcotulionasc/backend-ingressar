package br.com.multiprodutora.ticketeria.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Paging {
    private int total;
    private int limit;
    private int offset;


}