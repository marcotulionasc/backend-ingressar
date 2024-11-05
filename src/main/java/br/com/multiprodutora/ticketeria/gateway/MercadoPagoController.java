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
        MercadoPago.SDK.setAccessToken("TEST-3207297883527284-083117-715bbfa6ab0c180e5b699cbfb1ef4b54-1671033317");

        Preference preference = new Preference();

        Item item = new Item();
        item.setTitle(paymentRequest.getEventName())
                .setQuantity(paymentRequest.getBuyQuantity())
                .setUnitPrice(paymentRequest.getTicketPriceTotal())
                .setCurrencyId("BRL");

        preference.appendItem(item);

        BackUrls backUrls = new BackUrls();
        backUrls.setSuccess("http://ingressonaingressar.vercel.app/success.html")
                .setFailure("http://localhost:8080/failure.html") // Url of test
                .setPending("http://localhost:8080/pending.html");  // Url of test

        preference.setBackUrls(backUrls);
        preference.setAutoReturn(Preference.AutoReturn.valueOf("approved"));
        preference.save();

        String userId = paymentRequest.getUserId();

        return preference.getId();

    }
}
