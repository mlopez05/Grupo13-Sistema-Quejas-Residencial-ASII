package com.portal.residencial.authservice.casos;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.portal.residencial.authservice.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/* CONSULTA A CASOS-SERVICE */
@Component
@RequiredArgsConstructor
@Slf4j
public class CasosClient {

    private final RestTemplate restTemplate;

    @Value("${casos.service.url}")
    private String casosUrl;

    /* CUÁNTOS CASOS EN CURSO TIENE EL ANALISTA; SI NO RESPONDE, NO SE PERMITE CONTINUAR */
    public long contarCasosEnCurso(Long idAnalista) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, tokenDeLaPeticion());

            Map<String, Long> respuesta = restTemplate.exchange(
                    casosUrl + "/api/casos/analistas/" + idAnalista + "/casos-en-curso",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<Map<String, Long>>() {}).getBody();

            return respuesta == null ? 0 : respuesta.getOrDefault("casosEnCurso", 0L);
        } catch (Exception e) {
            log.error("No se pudo consultar los casos del analista {}: {}", idAnalista, e.getMessage());
            throw new BusinessException(
                    "No se pudo verificar si el analista tiene casos asignados. Intente más tarde.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /* TOKEN DEL ADMINISTRADOR QUE HACE LA PETICIÓN */
    private static String tokenDeLaPeticion() {
        ServletRequestAttributes atributos = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return atributos == null ? null : atributos.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
    }
}
