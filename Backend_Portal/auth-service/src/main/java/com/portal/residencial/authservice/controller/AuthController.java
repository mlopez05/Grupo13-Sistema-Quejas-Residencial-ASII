package com.portal.residencial.authservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.residencial.authservice.dto.LoginRequest;
import com.portal.residencial.authservice.dto.LoginResponse;
import com.portal.residencial.authservice.service.JwtService;
import com.portal.residencial.authservice.service.UsuarioDetailsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usuario(), request.clave())
        );

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(request.usuario());
        String token = jwtService.generarToken(userDetails);

        String rol = userDetails.getAuthorities().iterator().next().getAuthority();

        return ResponseEntity.ok(new LoginResponse(token, request.usuario(), rol));
    }
}
