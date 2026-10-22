package com.portal.residencial.authservice.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.portal.residencial.authservice.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    private static final String CLAIM_TIPO = "tipo";
    private static final String TIPO_RECUPERACION = "RECUPERACION";

    public String generarToken(UserDetails userDetails) {
        var builder = Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities());

        /* DATOS DEL USUARIO QUE VIAJAN EN EL TOKEN */
        if (userDetails instanceof Usuario usuario) {
            builder = builder
                    .claim("id", usuario.getId())
                    .claim("nombre", usuario.getNombre())
                    .claim("apellido", usuario.getApellido())
                    .claim("rol", usuario.getRol().getNombre())
                    /* INDICA SI DEBE CAMBIAR SU CONTRASEÑA TEMPORAL */
                    .claim("debeCambiarClave", usuario.requiereCambioClave());
        }

        return builder
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    /* EL TOKEN DEL ENLACE DE RECUPERACIÓN NO SIRVE COMO SESIÓN */
    public boolean esTokenValido(String token, UserDetails userDetails) {
        Claims claims = extraerClaims(token);
        return claims.getSubject().equals(userDetails.getUsername())
                && claims.get(CLAIM_TIPO) == null
                && !estaExpirado(token);
    }

    /* SOLO ACEPTA TOKENS DEL ENLACE DE RECUPERACIÓN */
    public Claims validarTokenRecuperacion(String token) {
        Claims claims = extraerClaims(token);

        if (!TIPO_RECUPERACION.equals(claims.get(CLAIM_TIPO))) {
            throw new JwtException("El token no es de recuperación.");
        }

        return claims;
    }

    private boolean estaExpirado(String token) {
        return extraerClaims(token).getExpiration().before(new Date());
    }

    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generarTokenRecuperacion(String correo) {
        return Jwts.builder()
                .subject(correo)
                .claim(CLAIM_TIPO, TIPO_RECUPERACION)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1800000)) 
                .signWith(getSigningKey())
                .compact();
    }
}
