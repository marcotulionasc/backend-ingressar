package br.com.multiprodutora.ticketeria.application.user.repository;

import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByTenant(Tenant tenant);

    Optional<Object> findByIdAndTenant(Long userId, Tenant tenant);
}
