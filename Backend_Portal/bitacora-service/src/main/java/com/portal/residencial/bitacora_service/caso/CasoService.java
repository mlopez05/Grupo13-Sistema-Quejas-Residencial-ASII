package com.portal.residencial.bitacora_service.caso;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CasoService {
    private final CasoRepository casoRepository;

    public Caso guardarBitacora(CasoRequest request) {
        Caso registro = Caso.builder()
            .id_caso(request.idCaso())
            .numero_caso(request.numeroCaso())
            .tipo_evento(request.tipoEvento())
            .estado_anterior(request.estadoAnterior())
            .estado_nuevo(request.estadoNuevo())
            .descripcion(request.descripcion())
            .fecha(LocalDate.now())
            .hora(LocalTime.now())
            .ip_usuario(request.ipUsuario())
            .id_usuario(request.idUsuario())
            .nombre_usuario(request.nombreUsuario())
            .apellido_usuario(request.apellidoUsuario())
            .rol_usuario(request.rolUsuario())
            .build();

        return casoRepository.save(registro);
    }
}
