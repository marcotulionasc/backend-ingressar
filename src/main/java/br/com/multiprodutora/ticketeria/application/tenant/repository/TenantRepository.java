package br.com.multiprodutora.ticketeria.application.tenant.repository;

import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Tenant findByEmailAndPassword(String email, String password);

    Tenant findByEmail(String email);

}
