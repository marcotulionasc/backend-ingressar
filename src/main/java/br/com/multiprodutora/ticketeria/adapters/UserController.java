package br.com.multiprodutora.ticketeria.adapters;

import br.com.multiprodutora.ticketeria.config.ApiConfig;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.user.User;
import br.com.multiprodutora.ticketeria.domain.model.user.dto.CreateUserDTO;
import br.com.multiprodutora.ticketeria.repository.TenantRepository;
import br.com.multiprodutora.ticketeria.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApiConfig apiConfig;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/tenants/{tenantId}/user/create")
    public ResponseEntity<CreateUserDTO> createUser(@PathVariable Long tenantId, @RequestBody CreateUserDTO data, UriComponentsBuilder uriBuilder) {
        logger.info("Received request to create user for tenantId: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> {
            logger.error("Tenant not found for tenantId: {}", tenantId);
            return new RuntimeException("Tenant not found");
        });

        User user = new User(data);
        user.setTenant(tenant);

        userRepository.save(user);
        logger.info("User created successfully for tenantId: {}", tenantId);

        URI location = uriBuilder.path("/tenants/{tenantId}/users/{userId}").buildAndExpand(tenant.getId(), user.getId()).toUri();
        return ResponseEntity.created(location).body(data);
    }

    @Transactional
    @PostMapping("/tenants/{tenantId}/users/login")
    public ResponseEntity<Map<String, String>> login(@PathVariable Long tenantId, @RequestBody CreateUserDTO data) {
        logger.info("Received login request for tenantId: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> {
            logger.error("Tenant not found for tenantId: {}", tenantId);
            return new RuntimeException("Tenant not found");
        });

        User user = userRepository.findByEmail(data.email()).orElseThrow(() -> {
            logger.error("User not found with email: {}", data.email());
            return new RuntimeException("User not found");
        });

        if (user.getPassword().equals(data.password())) {
            logger.info("Login successful for user email: {}", data.email());

            Map<String, String> response = new HashMap<>();
            response.put("name", user.getName());
            response.put("email", user.getEmail());

            String imageUrl = user.getImageProfileBase64() != null ? apiConfig.getApiBaseUrl() + user.getImageProfileBase64() : "https://via.placeholder.com/300x150.png?text=Imagem+Indisponível";
            response.put("imageProfileBase64", imageUrl);

            return ResponseEntity.ok(response);
        } else {
            logger.warn("Incorrect password for user email: {}", data.email());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Incorrect password"));
        }
    }

    @Transactional
    @GetMapping("/tenants/{tenantId}/users")
    public ResponseEntity<Iterable<Map<String, Object>>> listUsersByTenant(@PathVariable Long tenantId) {
        logger.info("Received request to list users for tenantId: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> {
            logger.error("Tenant not found for tenantId: {}", tenantId);
            return new RuntimeException("Tenant not found");
        });

        Iterable<User> users = userRepository.findByTenant(tenant);

        List<Map<String, Object>> response = getMaps(users);

        logger.info("Users listed successfully for tenantId: {}", tenantId);
        return ResponseEntity.ok(response);
    }

    private List<Map<String, Object>> getMaps(Iterable<User> users) {
        List<Map<String, Object>> response = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> userResponse = new HashMap<>();
            userResponse.put("id", user.getId());
            userResponse.put("name", user.getName());
            userResponse.put("email", user.getEmail());
            String imageUrl = user.getImageProfileBase64() != null
                    ? apiConfig.getApiBaseUrl() + user.getImageProfileBase64()
                    : "https://via.placeholder.com/300x150.png?text=Imagem+Indisponível";
            userResponse.put("imageProfileBase64", imageUrl);
            response.add(userResponse);
        }
        return response;
    }

}
