package br.com.multiprodutora.ticketeria.adapters;

import br.com.multiprodutora.ticketeria.domain.model.user.User;
import br.com.multiprodutora.ticketeria.repository.UserRepository;
import br.com.multiprodutora.ticketeria.service.JavaSmtpGmailSenderService;
import br.com.multiprodutora.ticketeria.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/tenants/{tenantId}/users")
public class PasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JavaSmtpGmailSenderService emailService;

    private final Logger logger = LoggerFactory.getLogger(PasswordController.class);

    // Exibir a página de solicitação de redefinição de senha
    @GetMapping("/forgot-password")
    public ResponseEntity<String> showForgotPasswordPage(@PathVariable Long tenantId) {
        String htmlPage = String.format("""
        <!DOCTYPE html>
        <html lang="pt-BR">
        <head>
            <meta charset="UTF-8">
            <title>Redefinir Senha</title>
            <script src="https://cdn.tailwindcss.com"></script>
        </head>
        <body class="bg-gray-900 flex items-center justify-center min-h-screen">
            <div class="bg-gray-800 p-8 rounded shadow-md text-center w-full max-w-md">
                <h1 class="text-3xl font-bold text-white mb-6">Redefinir Senha</h1>
                <form action="/tenants/%d/users/forgot-password" method="post">
                    <input type="email" name="email" placeholder="Digite seu email" required
                        class="w-full p-3 mb-4 rounded bg-gray-700 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-600">
                    <button type="submit"
                        class="w-full bg-blue-600 hover:bg-blue-800 text-white font-bold py-2 px-4 rounded">
                        Enviar Link de Redefinição
                    </button>
                </form>
            </div>
        </body>
        </html>
        """, tenantId);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(htmlPage);
    }

    // Processar o formulário de solicitação de redefinição de senha
    @PostMapping("/forgot-password")
    public ResponseEntity<String> processForgotPassword(
            @PathVariable Long tenantId,
            @RequestParam String email) {
        logger.info("Received password reset request for tenantId: {} and email: {}", tenantId, email);

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            logger.error("User not found for email: {}", email);
            String htmlError = String.format("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <title>Usuário Não Encontrado</title>
                <script src="https://cdn.tailwindcss.com"></script>
            </head>
            <body class="bg-gray-900 flex items-center justify-center min-h-screen">
                <div class="bg-gray-800 p-8 rounded shadow-md text-center w-full max-w-md">
                    <h1 class="text-2xl font-bold text-red-600 mb-4">Usuário Não Encontrado</h1>
                    <p class="mb-6 text-white">O email fornecido não está cadastrado.</p>
                    <a href="/tenants/%d/users/forgot-password" class="text-blue-500 hover:underline">
                        Tentar Novamente
                    </a>
                </div>
            </body>
            </html>
            """, tenantId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlError);
        }

        User user = optionalUser.get();

        // Gerar token de redefinição de senha
        String resetToken = tokenService.generateToken(user, false); // Passar o segundo parâmetro 'false'

        // Enviar email com o link de redefinição
        String resetLink = String.format("https://backend-ingressar.onrender.com/tenants/%d/users/reset-password?token=%s", tenantId, resetToken);
        String subject = "Redefinição de Senha";
        String body = "Clique no link para redefinir sua senha:\n" + resetLink;
        emailService.sendEmail(user.getEmail(), subject, body);

        logger.info("Password reset email sent to {}", email);

        String htmlSuccess = """
        <!DOCTYPE html>
        <html lang="pt-BR">
        <head>
            <meta charset="UTF-8">
            <title>Redefinição Enviada</title>
            <script src="https://cdn.tailwindcss.com"></script>
        </head>
        <body class="bg-gray-900 flex items-center justify-center min-h-screen">
            <div class="bg-gray-800 p-8 rounded shadow-md text-center w-full max-w-md">
                <h1 class="text-2xl font-bold text-white mb-4">Email Enviado!</h1>
                <p class="mb-6 text-white">Um link para redefinir sua senha foi enviado para seu email.</p>
                <a href="https://ingressonaingressar.vercel.app/index.html" class="text-blue-500 hover:underline">
                    Voltar para Início
                </a>
            </div>
        </body>
        </html>
        """;

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(htmlSuccess);
    }

    // Exibir a página para redefinir a senha
    @GetMapping("/reset-password")
    public ResponseEntity<String> showResetPasswordPage(
            @PathVariable Long tenantId,
            @RequestParam String token) {
        if (!tokenService.validateToken(token)) {
            String htmlError = String.format("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <title>Token Inválido</title>
                <script src="https://cdn.tailwindcss.com"></script>
            </head>
            <body class="bg-gray-900 flex items-center justify-center min-h-screen">
                <div class="bg-gray-800 p-8 rounded shadow-md text-center w-full max-w-md">
                    <h1 class="text-2xl font-bold text-red-600 mb-4">Token Inválido ou Expirado</h1>
                    <p class="mb-6 text-white">O link de redefinição não é válido ou já foi utilizado.</p>
                    <a href="https://backend-ingressar.onrender.com/tenants/%d/users/forgot-password" class="text-blue-500 hover:underline">
                        Solicitar Novo Link
                    </a>
                </div>
            </body>
            </html>
            """, tenantId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlError);
        }

        String htmlPage = String.format("""
        <!DOCTYPE html>
        <html lang="pt-BR">
        <head>
            <meta charset="UTF-8">
            <title>Redefinir Senha</title>
            <script src="https://cdn.tailwindcss.com"></script>
        </head>
        <body class="bg-gray-900 flex items-center justify-center min-h-screen">
            <div class="bg-gray-800 p-8 rounded shadow-md text-center w-full max-w-md">
                <h1 class="text-3xl font-bold text-white mb-6">Criar Nova Senha</h1>
                <form action="https://backend-ingressar.onrender.com/tenants/%d/users/reset-password" method="post">
                    <input type="hidden" name="token" value="%s">
                    <input type="password" name="newPassword" placeholder="Nova Senha" required
                        class="w-full p-3 mb-4 rounded bg-gray-700 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-600">
                    <input type="password" name="confirmPassword" placeholder="Confirmar Senha" required
                        class="w-full p-3 mb-6 rounded bg-gray-700 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-600">
                    <button type="submit"
                        class="w-full bg-blue-600 hover:bg-blue-800 text-white font-bold py-2 px-4 rounded">
                        Redefinir Senha
                    </button>
                </form>
            </div>
        </body>
        </html>
        """, tenantId, token);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(htmlPage);
    }

    // Processar a redefinição de senha
    @PostMapping("/reset-password")
    public ResponseEntity<String> processResetPassword(
            @PathVariable Long tenantId,
            @RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword) {
        if (!tokenService.validateToken(token)) {
            String htmlError = String.format("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <title>Token Inválido</title>
                <script src="https://cdn.tailwindcss.com"></script>
            </head>
            <body class="bg-gray-900 flex items-center justify-center min-h-screen">
                <div class="bg-gray-800 p-8 rounded shadow-md text-center w-full max-w-md">
                    <h1 class="text-2xl font-bold text-red-600 mb-4">Token Inválido ou Expirado</h1>
                    <p class="mb-6 text-white">O link de redefinição não é válido ou já foi utilizado.</p>
                    <a href="https://backend-ingressar.onrender.com/tenants/%d/users/forgot-password" class="text-blue-500 hover:underline">
                        Solicitar Novo Link
                    </a>
                </div>
            </body>
            </html>
            """, tenantId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlError);
        }

        if (!newPassword.equals(confirmPassword)) {
            String htmlError = String.format("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <title>Senhas Não Coincidem</title>
                <script src="https://cdn.tailwindcss.com"></script>
            </head>
            <body class="bg-gray-900 flex items-center justify-center min-h-screen">
                <div class="bg-gray-800 p-8 rounded shadow-md text-center w-full max-w-md">
                    <h1 class="text-2xl font-bold text-red-600 mb-4">As Senhas Não Coincidem</h1>
                    <p class="mb-6 text-white">Por favor, certifique-se de que as senhas inseridas são iguais.</p>
                    <a href="https://backend-ingressar.onrender.com/tenants/%d/users/reset-password?token=%s" class="text-blue-500 hover:underline">
                        Tentar Novamente
                    </a>
                </div>
            </body>
            </html>
            """, tenantId, token);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlError);
        }

        String email = tokenService.getEmailFromToken(token); // Método que precisamos adicionar

        if (email == null || email.isEmpty()) {
            logger.error("Invalid token provided");
            String htmlError = String.format("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <title>Token Inválido</title>
                <script src="https://cdn.tailwindcss.com"></script>
            </head>
            <body class="bg-gray-900 flex items-center justify-center min-h-screen">
                <div class="bg-gray-800 p-8 rounded shadow-md text-center w-full max-w-md">
                    <h1 class="text-2xl font-bold text-red-600 mb-4">Token Inválido ou Expirado</h1>
                    <p class="mb-6 text-white">O link de redefinição não é válido ou já foi utilizado.</p>
                    <a href="https://backend-ingressar.onrender.com/tenants/%d/users/forgot-password" class="text-blue-500 hover:underline">
                        Solicitar Novo Link
                    </a>
                </div>
            </body>
            </html>
            """, tenantId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlError);
        }

        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            logger.error("User not found for email: {}", email);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        });


        user.setPassword(newPassword);

        userRepository.save(user);

        logger.info("Password reset successfully for user: {}", email);

        String htmlSuccess = """
        <!DOCTYPE html>
        <html lang="pt-BR">
        <head>
            <meta charset="UTF-8">
            <title>Senha Redefinida</title>
            <script src="https://cdn.tailwindcss.com"></script>
        </head>
        <body class="bg-gray-900 flex items-center justify-center min-h-screen">
            <div class="bg-gray-800 p-8 rounded shadow-md text-center w-full max-w-md">
                <h1 class="text-2xl font-bold text-white mb-4">Senha Redefinida com Sucesso!</h1>
                <p class="mb-6 text-white">Agora você pode fazer login com sua nova senha.</p>
                <a href="https://ingressonaingressar.vercel.app/index.html" class="text-blue-500 hover:underline">
                    Voltar para Início
                </a>
            </div>
        </body>
        </html>
        """;

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(htmlSuccess);
    }
}
