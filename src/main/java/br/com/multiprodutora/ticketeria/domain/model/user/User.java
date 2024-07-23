package br.com.multiprodutora.ticketeria.domain.model.user;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.address.Address;
import br.com.multiprodutora.ticketeria.domain.model.shoppingCart.ShoppingCart;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.user.dto.CreateUserDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Base64;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String birthDate;
    private String email;
    private String password;
    private String cpf;
    private String phone;
    private String imageProfileBase64;
    private Status isUserActive;
    private LocalDateTime createdAt;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ShoppingCart> shoppingCarts;

    @ManyToOne
    @JoinColumn(name = "tenant_id") // Foreign key column
    private Tenant tenant;

    public User(CreateUserDTO data) {
        this.name = data.name();
        this.birthDate = data.birthDate();
        this.email = data.email();
        this.password = data.password();
        this.cpf = data.cpf();
        this.phone = data.phone();
        this.imageProfileBase64 = data.imageProfileBase64();
        this.isUserActive = Status.ACTIVE;
    }
}
