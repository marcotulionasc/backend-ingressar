package br.com.multiprodutora.ticketeria.domain.model.address;

import br.com.multiprodutora.ticketeria.domain.model.address.dto.AddressDTO;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private String street;
    private String neighborhood;
    private String numberAddress;
    private String cep;
    private UF uf;
    private String complement;
    private String city;

    public Address(AddressDTO data) {

        this.street = data.street() != null ? data.street() : "";
        this.neighborhood = data.neighborhood() != null ? data.neighborhood() : "";
        this.numberAddress = data.numberAddress() != null ? data.numberAddress() : "";
        this.cep = data.cep() != null ? data.cep() : "";
        this.uf = data.uf() != null ? data.uf() : UF.valueOf("");
        this.complement = data.complement() != null ? data.complement() : "";
        this.city = data.city() != null ? data.city() : "";
    }

    @Override
    public String toString() {
        return street + ", " + neighborhood + " - " + city + " - " + uf;
    }
}
