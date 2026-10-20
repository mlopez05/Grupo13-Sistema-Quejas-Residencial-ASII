package com.portal.residencial.bitacora_service.documento;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentoService {
    private final DocumentoRepository documentoRepository;

    public Documento guardarBitacora(DocumentoRequest request) {
        Documento registro = Documento.builder()
            .id_documento(request.idDocumento())
            .id_caso(request.idCaso())
            .numero_caso(request.numeroCaso())
            .nombre_archivo(request.nombreArchivo())
            .tipo_archivo(request.tipoArchivo())
            .tamano_bytes(request.tamanoBytes())
            .tipo_evento(request.tipoEvento())
            .fecha(LocalDate.now())
            .hora(LocalTime.now())
            .ip_usuario(request.ipUsuario())
            .id_usuario(request.idUsuario())
            .nombre_usuario(request.nombreUsuario())
            .apellido_usuario(request.apellidoUsuario())
            .rol_usuario(request.rolUsuario())
            .build();

        return documentoRepository.save(registro);
    }
}
