package com.portal.residencial.bitacora_service.usuario;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor  
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public Usuario guadarBitacora(UsuarioRequest request){
        Usuario registro = Usuario.builder()
            .id_usuario_modificado(request.idUsuarioModificado())
            .nombre_usuario_modificado(request.nombreUsuarioModificado())
            .apellido_usuario_modificado(request.apellidoUsuarioModificado())
            .campo_modificado(request.campoModificado())
            .valor_anterior(request.valorAnterior())
            .valor_nuevo(request.valorNuevo())
            .fecha(LocalDate.now())
            .hora(LocalTime.now())
            .tipo_evento(request.tipoEvento())
            .ip_usuario_modificador(request.ipUsuarioModificador())
            .id_usuario_modificador(request.idUsuarioModificador())
            .nombre_usuario_modificador(request.nombreUsuarioModificador())
            .apellido_usuario_modificador(request.apellidoUsuarioModificador())
            .build();

        return usuarioRepository.save(registro);
    }
}
