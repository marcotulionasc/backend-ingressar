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
import jakarta.validation.constraints.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
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

    @Transactional
    @PostMapping("/tenants/{tenantId}/user/create")
    public ResponseEntity<Map<String, String>> createUser(
            @PathVariable Long tenantId,
            @RequestBody CreateUserDTO data,
            UriComponentsBuilder uriBuilder) {
        logger.info("Received request to create user for tenantId: {}", tenantId);

        if (data.email() == null || data.email().isEmpty()) {
            logger.error("Email é obrigatório.");
            return ResponseEntity.badRequest().body(Map.of("error", "Email é obrigatório."));
        }
        if (data.password() == null || data.password().isEmpty()) {
            logger.error("Senha é obrigatória.");
            return ResponseEntity.badRequest().body(Map.of("error", "Senha é obrigatória."));
        }
        if (data.name() == null || data.name().isEmpty()) {
            logger.error("Nome é obrigatório.");
            return ResponseEntity.badRequest().body(Map.of("error", "Nome é obrigatório."));
        }

        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> {
            logger.error("Tenant not found for tenantId: {}", tenantId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant não encontrado");
        });

        Optional<User> optionalUser = userRepository.findByEmail(data.email());
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            Status status = existingUser.getIsUserActive();

            if (status == Status.PENDING) {
                logger.warn("Tentativa de cadastro com email pendente: {}", data.email());

                String token = tokenService.generateToken(existingUser, false);
                try {
                    javaSmtpGmailSenderService.sendEmail(
                            existingUser.getEmail(),
                            "Bem-vindo ao " + tenant.getName(),
                            "Olá " + existingUser.getName() + ",\n\n" +
                                    "Parece que você já iniciou o cadastro em nossa plataforma.\n" +
                                    "Por favor, ative sua conta clicando no link: " +
                                    apiConfig.getApiBaseUrl() + "/api/tenants/" + tenantId + "/users/" + existingUser.getId() + "/activate?token=" + token + "\n\n" +
                                    "Atenciosamente,\n" +
                                    "Equipe Ingressar"
                    );
                    logger.info("Email de ativação reenviado para: {}", existingUser.getEmail());
                } catch (Exception e) {
                    logger.error("Erro ao reenviar email de ativação para: {}", existingUser.getEmail(), e);
                }

                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "error", "Email já cadastrado e pendente de validação. Verifique seu email para ativar a conta."
                ));
            } else if (status == Status.ACTIVE) {
                logger.warn("Tentativa de cadastro com email ativo: {}", data.email());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "error", "Email já cadastrado e ativo. Por favor, faça login."
                ));
            } else {
                logger.warn("Tentativa de cadastro com email de status desconhecido: {}", data.email());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "error", "Email já cadastrado com status desconhecido. Por favor, entre em contato com o suporte."
                ));
            }
        }

        User user = new User();
        user.setEmail(data.email());
        user.setName(data.name());
        user.setPassword(data.password());
        user.setTenant(tenant);
        user.setIsUserActive(Status.PENDING);
        userRepository.save(user);
        logger.info("User created successfully for tenantId: {}", tenantId);

        String token = tokenService.generateToken(user, false);

        try {
            javaSmtpGmailSenderService.sendEmail(
                    user.getEmail(),
                    "Bem-vindo ao " + tenant.getName(),
                    "Olá " + user.getName() + ",\n\n" +
                            "Seja bem-vindo ao Ticketeria! Agradecemos por se cadastrar em nossa plataforma.\n\n" +
                            "Atenciosamente,\n" +
                            "Equipe Ingressar\n\n" +
                            "Ative sua conta clicando no link: " +
                            apiConfig.getApiBaseUrl() + "/api/tenants/" + tenantId + "/users/" + user.getId() + "/activate?token=" + token
            );
            logger.info("Email de ativação enviado para: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Erro ao enviar email de ativação para: {}", user.getEmail(), e);
        }

        URI location = uriBuilder.path("/tenants/{tenantId}/users/{userId}")
                .buildAndExpand(tenant.getId(), user.getId()).toUri();

        return ResponseEntity.created(location).body(Map.of(
                "message", "Usuário cadastrado com sucesso. Verifique seu email para ativar a conta."
        ));
    }
    @GetMapping("/tenants/{tenantId}/users/{userId}/activate")
    public ResponseEntity<String> activateUser(
            @PathVariable Long tenantId,
            @PathVariable Long userId,
            @RequestParam String token) {
        logger.info("Received request to activate user for tenantId: {} and userId: {}", tenantId, userId);

        User user = userRepository.findById(userId).orElseThrow(() -> {
            logger.error("User not found for userId: {}", userId);
            String htmlUserNotFound = """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <title>Usuário Não Encontrado</title>
                <script src="https://cdn.tailwindcss.com"></script>
            </head>
            <body class="bg-gray-100 flex items-center justify-center h-screen">
                <div class="bg-white p-8 rounded shadow-md text-center">
                    <h1 class="text-2xl font-bold text-red-600 mb-4">Usuário Não Encontrado</h1>
                    <p class="mb-6">O usuário que você está tentando ativar não existe.</p>
                    <a href="https://ingressonaingressar.vercel.app/index.html" class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
                        Voltar para Início
                    </a>
                </div>
            </body>
            </html>
        """;
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", new RuntimeException(htmlUserNotFound));
        });

        if (tokenService.validateToken(token)) {
            user.setIsUserActive(Status.ACTIVE);
            userRepository.save(user);
            logger.info("User activated successfully for tenantId: {} and userId: {}", tenantId, userId);

            // Definir o conteúdo HTML de sucesso
            String htmlSuccess = """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <title>Ativação de Usuário</title>
                <script src="https://cdn.tailwindcss.com"></script>
            </head>
            <body class="bg-gray-950 flex items-center justify-center h-screen">
                <div class="bg-gray-800 p-8 rounded shadow-md text-center">
                    <h1 class="text-3xl font-bold text-white mb-4">Usuário Ativado com Sucesso!</h1>
                    <p class="mb-6 text-white">Obrigado por validar seu e-mail. Agora você pode fazer login.</p>
                    <a href="https://ingressonaingressar.vercel.app/index.html" class="bg-blue-600 hover:bg-blue-800 text-white font-bold py-2 px-4 rounded">
                        Voltar para Início
                    </a>
                </div>
            </body>
            </html>
        """;

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlSuccess);
        } else {
            logger.error("Invalid token for user activation for tenantId: {} and userId: {}", tenantId, userId);

            String htmlError = """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <title>Ativação de Usuário</title>
                <script src="https://cdn.tailwindcss.com"></script>
            </head>
            <body class="bg-gray-100 flex items-center justify-center h-screen">
                <div class="bg-white p-8 rounded shadow-md text-center">
                    <h1 class="text-3xl font-bold text-red-600 mb-4">Token Inválido ou Expirado</h1>
                    <p class="mb-6">O link de ativação não é válido ou já foi utilizado.</p>
                    <a href="https://ingressonaingressar.vercel.app/index.html" class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
                        Voltar para Início
                    </a>
                </div>
            </body>
            </html>
        """;

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlError);
        }
    }

    @Transactional
    @PostMapping("/tenants/{tenantId}/users/login")
    public ResponseEntity<Map<String, String>> login(@PathVariable Long tenantId, @RequestBody CreateUserDTO data) {
        logger.info("Received login request for tenantId: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> {
            logger.error("Tenant not found for tenantId: {}", tenantId);
            return new RuntimeException("Tenant não encontrado");
        });

        Optional<User> optionalUser = userRepository.findByEmail(data.email());
        if (optionalUser.isEmpty()) {
            logger.warn("Email [{}] não cadastrado.", data.email());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Email não cadastrado."));
        }

        User user = optionalUser.get();

        if (user.getIsUserActive() == Status.PENDING) {
            logger.warn("Email [{}] pendente de validação.", data.email());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Cadastro pendente. Por favor, valide seu email acessando o link enviado."
            ));
        } else if (user.getIsUserActive() != Status.ACTIVE) {
            logger.warn("Status do usuário inválido para o email: {}", data.email());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Cadastro não está ativo. Entre em contato com o suporte."
            ));
        }

        if (!user.getPassword().equals(data.password())) {
            logger.warn("Senha incorreta para o email: {}", data.email());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Senha incorreta."));
        }

        String imageUrl = apiConfig.getApiBaseUrl() + user.getImageProfileBase64();
        Map<String, String> response = new HashMap<>();
        response.put("id", String.valueOf(user.getId()));
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("imageUrl", imageUrl);

        logger.info("Login realizado com sucesso para o email: {}", data.email());
        return ResponseEntity.ok(response);
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

            user.setAddress(address);
        }

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
