package br.com.multiprodutora.ticketeria.adapters;

import br.com.multiprodutora.ticketeria.config.ApiConfig;
import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.address.Address;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.user.User;
import br.com.multiprodutora.ticketeria.domain.model.user.dto.CreateUserDTO;
import br.com.multiprodutora.ticketeria.domain.model.user.dto.UpdateUserDTO;
import br.com.multiprodutora.ticketeria.repository.TenantRepository;
import br.com.multiprodutora.ticketeria.repository.UserRepository;
import br.com.multiprodutora.ticketeria.service.JavaSmtpGmailSenderService;
import br.com.multiprodutora.ticketeria.service.TokenService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
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
    private JavaSmtpGmailSenderService javaSmtpGmailSenderService;

    @Autowired
    private TokenService tokenService;

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

        var token = tokenService.generateToken(user, false);

        javaSmtpGmailSenderService.sendEmail(user.getEmail(), "Bem-vindo ao Ticketeria", "Olá " + user.getName() + ",\n\n" +
                "Seja bem-vindo ao Ticketeria! Agradecemos por se cadastrar em nossa plataforma.\n\n" +
                "Atenciosamente,\n" +
                "Equipe Ticketeria\n" +
                "Ative sua conta clicando no link: " + apiConfig.getApiBaseUrl() + "/api/tenants/" + tenantId + "/users/" + user.getId() + "/activate?token=" + token);

        URI location = uriBuilder.path("/tenants/{tenantId}/users/{userId}").buildAndExpand(tenant.getId(), user.getId()).toUri();
        return ResponseEntity.created(location).body(data);
    }

    @GetMapping("/tenants/{tenantId}/users/{userId}/activate")
    public ResponseEntity<Map<String, String>> activateUser(@PathVariable Long tenantId, @PathVariable Long userId, @RequestParam String token) {
        logger.info("Received request to activate user for tenantId: {} and userId: {}", tenantId, userId);

        User user = userRepository.findById(userId).orElseThrow(() -> {
            logger.error("User not found for userId: {}", userId);
            return new RuntimeException("User not found");
        });

        if (tokenService.validateToken(token)) {
            user.setIsUserActive(Status.ACTIVE);
            userRepository.save(user);
            logger.info("User activated successfully for tenantId: {} and userId: {}", tenantId, userId);
            return ResponseEntity.ok(Map.of("message", "User activated successfully"));
        } else {
            logger.error("Invalid token for user activation for tenantId: {} and userId: {}", tenantId, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid token"));
        }
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

        // TODO: TRATAR EXCEÇÃO DE USUÁRIO NÃO ATIVO
        if (user.getIsUserActive() != Status.ACTIVE) {
            logger.error("User is not active for email: {}", data.email());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "User is not active"));
        }

        String imageUrl = apiConfig.getApiBaseUrl() + user.getImageProfileBase64();

        logger.info("User password: {}", user.getPassword());
        logger.info("Data password: {}", data.password());

        // Verifica se a senha está correta
        if (user.getPassword().equals(data.password())) {
            logger.info("Login successful for user email: {}", data.email());

            Map<String, String> response = new HashMap<>();
            response.put("id", String.valueOf(user.getId()));
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("imageUrl", imageUrl);

            // Inicializa o RestTemplate para buscar a imagem
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            String base64image = "";

            if (user.getImageProfileBase64() != null) {
                try {
                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    ResponseEntity<byte[]> responseImg = restTemplate.exchange(imageUrl, HttpMethod.GET, entity, byte[].class);

                    if(responseImg.getStatusCode() != HttpStatus.OK){
                        logger.error("Imagem não encontrada para o email: {}", data.email());
                        response.put("imageBase64", "http://via.placeholder.com/300x150.png?text=Imagem+Indisponível");
                        return ResponseEntity.ok(response);
                    }

                    byte[] imageBytes = responseImg.getBody();

                    base64image = Base64.getEncoder().encodeToString(imageBytes);
                    logger.info("Imagem exibida com sucesso!");

                    response.put("imageBase64", base64image);
                } catch (Exception e) {

                    logger.error("Erro ao carregar a imagem do perfil para o email: {}", data.email(), e);
                    response.put("imageBase64", "http://via.placeholder.com/300x150.png?text=Imagem+Indisponível");
                }
            } else {
                response.put("imageBase64", "http://via.placeholder.com/300x150.png?text=Imagem+Indisponível");
            }

            return ResponseEntity.ok(response);
        } else {
            logger.warn("Incorrect password for user email: [{}], password: [{}]", data.email(), data.password());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Incorrect password"));
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

    @Transactional
    @GetMapping("/tenants/{tenantId}/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long tenantId, @PathVariable Long userId) {
        logger.info("Received request to get user for tenantId: {} and userId: {}", tenantId, userId);

        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> {
            logger.error("Tenant not found for tenantId: {}", tenantId);
            return new RuntimeException("Tenant not found");
        });

        User user = (User) userRepository.findByIdAndTenant(userId, tenant).orElseThrow(() -> {
            logger.error("User not found for userId: {}", userId);
            return new RuntimeException("User not found");
        });

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        String imageUrl = user.getImageProfileBase64() != null ? apiConfig.getApiBaseUrl() + user.getImageProfileBase64() : "https://via.placeholder.com/300x150.png?text=Imagem+Indisponível";
        response.put("imageProfileBase64", imageUrl);
        response.put("password", user.getPassword());
        response.put("cpf", user.getCpf());
        response.put("phone", user.getPhone());
        response.put("birthDate", user.getBirthDate());

        if (user.getAddress() != null) {
            response.put("street", user.getAddress().getStreet());
            response.put("neighborhood", user.getAddress().getNeighborhood());
            response.put("numberAddress", user.getAddress().getNumberAddress());
            response.put("cep", user.getAddress().getCep());
            response.put("uf", user.getAddress().getUf());
            response.put("complement", user.getAddress().getComplement());
            response.put("city", user.getAddress().getCity());
        } else {
            response.put("street", "");
            response.put("neighborhood", "");
            response.put("numberAddress", "");
            response.put("cep", "");
            response.put("uf", "");
            response.put("complement", "");
            response.put("city", "");
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/tenants/{tenantId}/users/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long tenantId, @PathVariable Long userId, @RequestBody UpdateUserDTO data) {
        logger.info("Received request to update user for tenantId: {} and userId: {}", tenantId, userId);

        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> {
            logger.error("Tenant not found for tenantId: {}", tenantId);
            return new RuntimeException("Tenant not found");
        });

        User user = (User) userRepository.findByIdAndTenant(userId, tenant).orElseThrow(() -> {
            logger.error("User not found for userId: {}", userId);
            return new RuntimeException("User not found");
        });

        if (data.name() != null) {
            user.setName(data.name());
        }
        if (data.email() != null) {
            user.setEmail(data.email());
        }
        if (data.password() != null) {
            user.setPassword(data.password());
        }
        if (data.cpf() != null) {
            user.setCpf(data.cpf());
        }
        if (data.phone() != null) {
            user.setPhone(data.phone());
        }
        if (data.imageProfileBase64() != null) {
            user.setImageProfileBase64(data.imageProfileBase64());
        }
        if (data.birthDate() != null) {
            user.setBirthDate(data.birthDate());
        }

        // Manter o createdAt intacto
        user.setCreatedAt(user.getCreatedAt());

        // Atualizar endereço se existir
        if (data.address() != null) {
            Address address = user.getAddress();
            if (address == null) {
                address = new Address();
            }
            if (data.address().getStreet() != null) {
                address.setStreet(data.address().getStreet());
            }
            if (data.address().getNeighborhood() != null) {
                address.setNeighborhood(data.address().getNeighborhood());
            }
            if (data.address().getNumberAddress() != null) {
                address.setNumberAddress(data.address().getNumberAddress());
            }
            if (data.address().getCep() != null) {
                address.setCep(data.address().getCep());
            }
            if (data.address().getUf() != null) {
                address.setUf(data.address().getUf());
            }
            if (data.address().getComplement() != null) {
                address.setComplement(data.address().getComplement());
            }
            if (data.address().getCity() != null) {
                address.setCity(data.address().getCity());
            }

            // Setar o endereço atualizado
            user.setAddress(address);
        }

        // Salvar o usuário atualizado
        userRepository.save(user);

        // Criar a resposta
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("password", user.getPassword());
        response.put("cpf", user.getCpf());
        response.put("phone", user.getPhone());
        response.put("imageProfileBase64", user.getImageProfileBase64());
        response.put("createdAt", user.getCreatedAt());
        response.put("birthDate", user.getBirthDate());

        if (user.getAddress() != null) {
            response.put("street", user.getAddress().getStreet());
            response.put("neighborhood", user.getAddress().getNeighborhood());
            response.put("numberAddress", user.getAddress().getNumberAddress());
            response.put("cep", user.getAddress().getCep());
            response.put("uf", user.getAddress().getUf());
            response.put("complement", user.getAddress().getComplement());
            response.put("city", user.getAddress().getCity());
        } else {
            response.put("street", "");
            response.put("neighborhood", "");
            response.put("numberAddress", "");
            response.put("cep", "");
            response.put("uf", "");
            response.put("complement", "");
            response.put("city", "");
        }

        logger.info("User updated data: {}", response);
        return ResponseEntity.ok(response);
    }


    private List<Map<String, Object>> getMaps(Iterable<User> users) {
        List<Map<String, Object>> response = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> userResponse = new HashMap<>();
            userResponse.put("id", user.getId());
            userResponse.put("name", user.getName());
            userResponse.put("email", user.getEmail());
            String imageUrl = user.getImageProfileBase64() != null ? apiConfig.getApiBaseUrl() + user.getImageProfileBase64() : "https://via.placeholder.com/300x150.png?text=Imagem+Indisponível";
            userResponse.put("imageProfileBase64", imageUrl);
            response.add(userResponse);
        }
        return response;
    }

}
