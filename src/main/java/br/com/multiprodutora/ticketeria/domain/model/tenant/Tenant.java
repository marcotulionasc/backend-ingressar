package br.com.multiprodutora.ticketeria.domain.model.tenant;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.tenant.dto.RegisterTenantDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "Tenant")
@Table(name = "tenants")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")

public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String subdomain;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    private Status isTenantActive;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> events;


    public Tenant(RegisterTenantDTO data) {
        this.name = data.name();
        this.subdomain = data.subdomain();
        this.email = data.email();
        this.password = data.password();
        this.createdAt = LocalDateTime.now();
        this.isTenantActive = Status.ACTIVE; // Business rule
    }

}
