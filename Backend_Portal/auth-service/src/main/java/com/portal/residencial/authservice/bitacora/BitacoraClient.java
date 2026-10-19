package com.portal.residencial.authservice.bitacora;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.portal.residencial.authservice.model.Usuario;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BitacoraClient {
    private final RestTemplate restTemplate;

    @Value("${bitacora.service.url}")
    private String bitacoraUrl;

    @Async
    public void registrarSesion(Usuario usuario, String ip, String tipoEvento) {
        try {
            SesionRequest request = new SesionRequest(
                    usuario.getId(),
                    ip,
                    usuario.getCorreo(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getRol().getNombre(),
                    tipoEvento);
            restTemplate.postForEntity(bitacoraUrl + "/bitacora/sesion/registrar", request, Void.class);
        } catch (Exception e) {
            log.error("No se pudo registrar el evento de sesión en bitácora: {}", e.getMessage());
        }
    }

    @Async
    public void registrarCambioUsuario(Long idUsuarioModificado, String nombreUsuarioModificado, String apellidoUsuarioModificado,
        String campoModificado, String valorAnterior, String valorNuevo, String tipoEvento, String ipUsuarioModificador,
        Long idUsuarioModificador, String nombreUsuarioModificador,String apellidoUsuarioModificador ) {
        try {
            UsuarioRequest request = new UsuarioRequest(
                idUsuarioModificado,
                nombreUsuarioModificado,
                apellidoUsuarioModificado,
                campoModificado,
                valorAnterior,
                valorNuevo,
                tipoEvento,
                ipUsuarioModificador,
                idUsuarioModificador,
                nombreUsuarioModificador,
                apellidoUsuarioModificador);
            restTemplate.postForEntity(bitacoraUrl + "/bitacora/usuario/registro", request, Void.class);
        } catch (Exception e) {
            log.error("No se pudo registrar el cambio de usuario en bitácora: {}", e.getMessage());
        }
    }
}
