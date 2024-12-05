package br.com.multiprodutora.ticketeria.service;

import br.com.multiprodutora.ticketeria.domain.model.user.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
public class TokenService {
    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(User user, boolean isLogin) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("auth0")
                    .withSubject(user.getEmail())
                    .withExpiresAt(generateExpirationDate(isLogin))
                    .sign(algorithm);
            log.info("Token generated: {}", token);
            return token;
        } catch (Exception ex) {
            log.error("Error generating token", ex);
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            log.info("Validating token {}", token);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return true;
        } catch (JWTVerificationException ex){
            log.error("Error validating token", ex);
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject(); // O email está armazenado no subject
        } catch (JWTVerificationException ex) {
            log.error("Erro ao obter email do token", ex);
            return null;
        }
    }


    private Date generateExpirationDate(boolean isLogin) {
        LocalDateTime expirationTime = isLogin ? LocalDateTime.now().plusDays(30) : LocalDateTime.now().plusMinutes(15);
        return Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
