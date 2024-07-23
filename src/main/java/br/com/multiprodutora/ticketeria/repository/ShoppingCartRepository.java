package br.com.multiprodutora.ticketeria.repository;

import br.com.multiprodutora.ticketeria.domain.model.shoppingCart.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
}
