package br.com.multiprodutora.ticketeria.application.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TicketPDFDTO {

    private String nomeEvento;
    private String dataEvento;
    private String aberturaPortas;
    private String localEvento;
    private String enderecoEvento;

    private Long idIngresso;
    private String nomeIngresso;
    private String areaIngresso;

    private Long idLoteAtivo;
    private String valorLote;
    private int quantidadeLote;
    private int taxaLote;

    private String dataCompra;
    private String nomeComprador;

    private String textoNoIngresso;
}
