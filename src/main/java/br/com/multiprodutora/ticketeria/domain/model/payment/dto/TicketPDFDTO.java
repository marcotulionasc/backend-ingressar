package br.com.multiprodutora.ticketeria.domain.model.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TicketPDFDTO {
    // Dados do evento
    private String nomeEvento;
    private String dataEvento;
    private String aberturaPortas;
    private String localEvento;
    private String enderecoEvento;

    // Dados do ingresso
    private Long idIngresso;
    private String nomeIngresso;
    private String areaIngresso;

    // Dados do lote
    private Long idLoteAtivo;
    private String valorLote;
    private int quantidadeLote;
    private int taxaLote;

    // Dados da compra
    private String dataCompra;
    private String nomeComprador;

    // Texto do ingresso
    private String textoNoIngresso;
}
