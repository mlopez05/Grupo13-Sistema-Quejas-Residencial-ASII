package com.portal.residencial.notification_service.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/* VALIDA LOS TOKENS DEL INICIO DE SESIÓN */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    /* DEVUELVE EL CORREO DEL USUARIO */
    public String extraerCorreo(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
