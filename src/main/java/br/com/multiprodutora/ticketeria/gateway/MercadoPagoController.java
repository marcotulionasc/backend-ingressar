package br.com.multiprodutora.ticketeria.gateway;

import br.com.multiprodutora.ticketeria.domain.model.payment.PaymentRequest;
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.Item;
import com.mercadopago.resources.datastructures.preference.BackUrls;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class MercadoPagoController {

    @PostMapping("/create-preference")
    public String createPreference(@RequestBody PaymentRequest paymentRequest) throws MPException {
        MercadoPago.SDK.setAccessToken("APP_USR-8935049088704556-091209-5cb0774e6b3b2c2a3075a992ae117395-254412224");

        Preference preference = new Preference();

        Item item = new Item();
        item.setTitle(paymentRequest.getEventName())
                .setQuantity(paymentRequest.getTicketQuantity())
                .setUnitPrice(paymentRequest.getTicketPrice())
                .setCurrencyId("BRL");

        preference.appendItem(item);

        BackUrls backUrls = new BackUrls();
        backUrls.setSuccess("http://localhost:8080/success.html") // Mudar para produção
                .setFailure("http://localhost:8080/failure.html") // Mudar para produção
                .setPending("http://localhost:8080/pending.html"); // Mudar para produção

        preference.setBackUrls(backUrls);
        preference.setAutoReturn(Preference.AutoReturn.valueOf("approved"));
        preference.save();

        String userId = paymentRequest.getUserId();

        return preference.getId();

    }
}
