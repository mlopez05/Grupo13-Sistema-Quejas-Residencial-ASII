package com.portal.residencial.authservice.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.portal.residencial.authservice.model.Usuario;
import com.portal.residencial.authservice.service.JwtService;
import com.portal.residencial.authservice.service.UsuarioDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7); 
        final String userEmail = jwtService.extraerUsername(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = usuarioDetailsService.loadUserByUsername(userEmail);

            /* UN USUARIO INACTIVO YA NO PUEDE ENTRAR AUNQUE TENGA SESIÓN */
            if (jwtService.esTokenValido(jwt, userDetails) && userDetails.isEnabled()) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                /* CON CONTRASEÑA TEMPORAL SOLO PUEDE CAMBIARLA O CERRAR SESIÓN */
                if (((Usuario) userDetails).requiereCambioClave()
                        && !RUTAS_CON_CLAVE_TEMPORAL.contains(request.getRequestURI())) {
                    responderClaveTemporal(response);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private static final Set<String> RUTAS_CON_CLAVE_TEMPORAL =
            Set.of("/api/auth/change-password", "/api/auth/logout");

    private void responderClaveTemporal(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":403,"
                + "\"mensaje\":\"Debe definir una nueva contraseña antes de continuar.\","
                + "\"timestamp\":\"" + LocalDateTime.now() + "\"}");
    }
}
