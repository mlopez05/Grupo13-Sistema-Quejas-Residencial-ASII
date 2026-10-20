package com.portal.residencial.casos_service.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/* VALIDA LOS TOKENS DEL INICIO DE SESIÓN */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    /* LEE LOS DATOS DEL USUARIO DEL TOKEN */
    public UsuarioAutenticado extraerUsuario(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Number id = claims.get("id", Number.class);
        String rol = claims.get("rol", String.class);

        if (id == null || rol == null) {
            return null;
        }

        /* CON CONTRASEÑA TEMPORAL NO PUEDE USAR EL PORTAL */
        if (Boolean.TRUE.equals(claims.get("debeCambiarClave", Boolean.class))) {
            return null;
        }

        return new UsuarioAutenticado(
                id.longValue(),
                claims.getSubject(),
                claims.get("nombre", String.class),
                claims.get("apellido", String.class),
                rol,
                token);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
