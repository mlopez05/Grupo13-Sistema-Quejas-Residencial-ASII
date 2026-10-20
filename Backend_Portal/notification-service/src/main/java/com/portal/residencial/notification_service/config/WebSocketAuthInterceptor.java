package com.portal.residencial.notification_service.config;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

/* VALIDA LA SESIÓN AL CONECTARSE A LAS ALERTAS */
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessagingException("Falta el token de autenticación.");
            }

            try {
                String correo = jwtService.extraerCorreo(authHeader.substring(7));
                accessor.setUser(new UsernamePasswordAuthenticationToken(correo, null, List.of()));
            } catch (JwtException | IllegalArgumentException e) {
                throw new MessagingException("Token inválido o expirado.");
            }
        }

        return message;
    }
}
