package com.portal.residencial.authservice.controller;

import com.portal.residencial.authservice.bitacora.BitacoraClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.residencial.authservice.dto.CorreoRequest;
import com.portal.residencial.authservice.dto.LoginRequest;
import com.portal.residencial.authservice.dto.LoginResponse;
import com.portal.residencial.authservice.dto.ResetPasswordRequest;
import com.portal.residencial.authservice.model.Usuario;
import com.portal.residencial.authservice.service.JwtService;
import com.portal.residencial.authservice.service.UsuarioDetailsService;
import com.portal.residencial.authservice.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final BitacoraClient bitacoraClient;
    private final AuthenticationManager authenticationManager;
    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usuario(), request.clave()));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(request.usuario());
        String token = jwtService.generarToken(userDetails);

        String rol = userDetails.getAuthorities().iterator().next().getAuthority();

        Usuario usuario = (Usuario) userDetails;
        bitacoraClient.registrarSesion(usuario, httpRequest.getRemoteAddr(), "INICIO_SESION");

        return ResponseEntity.ok(new LoginResponse(token, request.usuario(), rol));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {

        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        bitacoraClient.registrarSesion(usuario, httpRequest.getRemoteAddr(), "CIERRE_SESION");

        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody CorreoRequest request) {
        usuarioService.solicitarRecuperacion(request.usuario());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        usuarioService.restablecerPassword(request.token(), request.nuevaPassword());
        return ResponseEntity.ok().build();
    }
}
