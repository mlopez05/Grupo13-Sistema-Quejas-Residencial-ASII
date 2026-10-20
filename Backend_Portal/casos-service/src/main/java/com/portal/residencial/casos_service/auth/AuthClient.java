package com.portal.residencial.casos_service.auth;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

/* LISTA DEL PERSONAL ACTIVO */
@Component
@RequiredArgsConstructor
public class AuthClient {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authUrl;

    public List<PersonalResponse> listarPersonal(String rol, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        List<PersonalResponse> personal = restTemplate.exchange(
                authUrl + "/api/registro/personal?rol={rol}",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<PersonalResponse>>() {},
                rol).getBody();

        return personal == null ? List.of() : personal;
    }

    /* DATOS DEL RESIDENTE QUE INICIÓ SESIÓN */
    public UsuarioResponse obtenerPerfil(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return restTemplate.exchange(
                authUrl + "/api/registro/perfil",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UsuarioResponse.class).getBody();
    }
}
