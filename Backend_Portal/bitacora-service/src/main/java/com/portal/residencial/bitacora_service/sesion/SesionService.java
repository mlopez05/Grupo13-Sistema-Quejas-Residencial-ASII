package com.portal.residencial.bitacora_service.sesion;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor  
public class SesionService {
    private  final SesionRepository sesionRepository;

    public Sesion registroSesion(SesionRequest request){
        Sesion sesion = Sesion.builder()
                        .id_usuario(request.idUsuario())
                        .fecha(LocalDate.now())
                        .hora(LocalTime.now())
                        .ip_usuario(request.ipUsuario())
                        .correo_usuario(request.correoUsuario())
                        .nombre_usuario(request.nombreUsuario())
                        .apellido_usuario(request.apellidoUsuario())
                        .rol_usuario(request.rolUsuario())
                        .tipo_evento(request.tipoEvento())
                        .build();

        return sesionRepository.save(sesion);
                    
    }
}
