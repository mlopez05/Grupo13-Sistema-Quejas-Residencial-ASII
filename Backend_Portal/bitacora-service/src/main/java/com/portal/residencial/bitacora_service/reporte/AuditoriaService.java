package com.portal.residencial.bitacora_service.reporte;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.portal.residencial.bitacora_service.caso.Caso;
import com.portal.residencial.bitacora_service.caso.CasoRepository;
import com.portal.residencial.bitacora_service.documento.Documento;
import com.portal.residencial.bitacora_service.documento.DocumentoRepository;
import com.portal.residencial.bitacora_service.exportacion.ExportadorExcel;
import com.portal.residencial.bitacora_service.exportacion.ExportadorPdf;
import com.portal.residencial.bitacora_service.exportacion.TablaExportable;
import com.portal.residencial.bitacora_service.sesion.Sesion;
import com.portal.residencial.bitacora_service.sesion.SesionRepository;
import com.portal.residencial.bitacora_service.usuario.Usuario;
import com.portal.residencial.bitacora_service.usuario.UsuarioRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

/* REPORTE DE AUDITORÍA DE LAS BITÁCORAS */
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private static final int MAXIMO_FILAS_EXPORTACION = 10_000;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CasoRepository casoRepository;
    private final DocumentoRepository documentoRepository;
    private final ExportadorExcel exportadorExcel;
    private final ExportadorPdf exportadorPdf;

    public record ArchivoReporte(byte[] contenido, String nombre, String tipoMime) {}

    /* DATOS PARA CONSULTAR CADA BITÁCORA */
    private record Consulta<T>(
            String titulo,
            JpaSpecificationExecutor<T> repositorio,
            String campoId,
            List<String> camposTexto,
            Function<T, RegistroAuditoriaResponse> aFila,
            Supplier<List<String>> eventos) {}

    public PaginaResponse<RegistroAuditoriaResponse> consultar(String tipo, FiltroAuditoria filtro) {
        return consultar(consulta(tipo), filtro);
    }

    public List<String> eventos(String tipo) {
        return consulta(tipo).eventos().get();
    }

    public ArchivoReporte exportar(String tipo, FiltroAuditoria filtro, String formato) {
        Consulta<?> consulta = consulta(tipo);
        List<RegistroAuditoriaResponse> filas = todas(consulta, filtro);

        String titulo = "Reporte de auditoría — " + consulta.titulo();
        List<String> encabezado = describirFiltros(filtro, filas.size());
        List<TablaExportable> tablas = List.of(new TablaExportable(consulta.titulo(),
                List.of("Fecha", "Hora", "Usuario", "Rol", "Evento", "Detalle", "IP"),
                filas.stream().map(f -> Arrays.<Object>asList(
                        f.fecha() == null ? "" : f.fecha().format(FECHA),
                        f.hora() == null ? "" : f.hora().format(HORA),
                        f.usuario(), f.rol(), f.evento(), f.detalle(), f.ip())).toList()));

        String nombreBase = "auditoria-" + tipo.toLowerCase(Locale.ROOT) + "-" + LocalDateTime.now().toLocalDate();

        return switch (formato == null ? "" : formato.toLowerCase(Locale.ROOT)) {
            case "excel" -> new ArchivoReporte(exportadorExcel.generar(titulo, encabezado, tablas),
                    nombreBase + ".xlsx", ExportadorExcel.TIPO_MIME);
            case "pdf" -> new ArchivoReporte(exportadorPdf.generar(titulo, encabezado, tablas),
                    nombreBase + ".pdf", ExportadorPdf.TIPO_MIME);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato no soportado. Use 'excel' o 'pdf'.");
        };
    }

    /* BITÁCORAS */

    private Consulta<?> consulta(String tipo) {
        return switch (tipo == null ? "" : tipo.toLowerCase(Locale.ROOT)) {
            case "sesiones" -> new Consulta<Sesion>("Sesiones", sesionRepository, "id_sesion",
                    List.of("nombre_usuario", "apellido_usuario", "correo_usuario"),
                    s -> new RegistroAuditoriaResponse(s.getId_sesion(), s.getFecha(), s.getHora(),
                            persona(s.getNombre_usuario(), s.getApellido_usuario()) + correo(s.getCorreo_usuario()),
                            s.getRol_usuario(), s.getTipo_evento(), "", s.getIp_usuario()),
                    sesionRepository::listarTiposEvento);

            case "usuarios" -> new Consulta<Usuario>("Cambios de usuarios", usuarioRepository, "id_bitacora_usuario",
                    List.of("nombre_usuario_modificador", "apellido_usuario_modificador",
                            "nombre_usuario_modificado", "apellido_usuario_modificado"),
                    u -> new RegistroAuditoriaResponse(u.getId_bitacora_usuario(), u.getFecha(), u.getHora(),
                            persona(u.getNombre_usuario_modificador(), u.getApellido_usuario_modificador()),
                            null, u.getTipo_evento(), detalleUsuario(u), u.getIp_usuario_modificador()),
                    usuarioRepository::listarTiposEvento);

            case "casos" -> new Consulta<Caso>("Eventos de casos", casoRepository, "id_bitacora_caso",
                    List.of("nombre_usuario", "apellido_usuario", "numero_caso"),
                    c -> new RegistroAuditoriaResponse(c.getId_bitacora_caso(), c.getFecha(), c.getHora(),
                            persona(c.getNombre_usuario(), c.getApellido_usuario()),
                            c.getRol_usuario(), c.getTipo_evento(), detalleCaso(c), c.getIp_usuario()),
                    casoRepository::listarTiposEvento);

            case "documentos" -> new Consulta<Documento>("Documentos", documentoRepository, "id_bitacora_documento",
                    List.of("nombre_usuario", "apellido_usuario", "numero_caso", "nombre_archivo"),
                    d -> new RegistroAuditoriaResponse(d.getId_bitacora_documento(), d.getFecha(), d.getHora(),
                            persona(d.getNombre_usuario(), d.getApellido_usuario()),
                            d.getRol_usuario(), d.getTipo_evento(), detalleDocumento(d), d.getIp_usuario()),
                    documentoRepository::listarTiposEvento);

            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tipo de bitácora no válido. Use: sesiones, usuarios, casos o documentos.");
        };
    }

    /* CONSULTA CON FILTROS */

    private <T> PaginaResponse<RegistroAuditoriaResponse> consultar(Consulta<T> consulta, FiltroAuditoria filtro) {
        Page<T> pagina = consulta.repositorio().findAll(especificacion(consulta, filtro),
                PageRequest.of(filtro.paginaEfectiva(), filtro.tamanoEfectivo(), orden(consulta)));

        return new PaginaResponse<>(
                pagina.getContent().stream().map(consulta.aFila()).toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages());
    }

    private <T> List<RegistroAuditoriaResponse> todas(Consulta<T> consulta, FiltroAuditoria filtro) {
        return consulta.repositorio()
                .findAll(especificacion(consulta, filtro), PageRequest.of(0, MAXIMO_FILAS_EXPORTACION, orden(consulta)))
                .getContent().stream()
                .map(consulta.aFila())
                .toList();
    }

    private <T> Specification<T> especificacion(Consulta<T> consulta, FiltroAuditoria filtro) {
        if (filtro.desde() != null && filtro.hasta() != null && filtro.hasta().isBefore(filtro.desde())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha final no puede ser anterior a la inicial.");
        }

        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();

            if (filtro.desde() != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("fecha"), filtro.desde()));
            }
            if (filtro.hasta() != null) {
                condiciones.add(cb.lessThanOrEqualTo(root.get("fecha"), filtro.hasta()));
            }
            if (filtro.evento() != null && !filtro.evento().isBlank()) {
                condiciones.add(cb.equal(root.get("tipo_evento"), filtro.evento()));
            }
            if (filtro.usuario() != null && !filtro.usuario().isBlank()) {
                String patron = "%" + filtro.usuario().trim().toLowerCase(Locale.ROOT) + "%";
                condiciones.add(cb.or(consulta.camposTexto().stream()
                        .map(campo -> cb.like(cb.lower(cb.coalesce(root.get(campo), "")), patron))
                        .toArray(Predicate[]::new)));
            }

            return cb.and(condiciones.toArray(Predicate[]::new));
        };
    }

    private static Sort orden(Consulta<?> consulta) {
        return Sort.by(Sort.Direction.DESC, "fecha", "hora", consulta.campoId());
    }

    /* TEXTOS DE CADA FILA */

    private static String detalleUsuario(Usuario u) {
        String afectado = persona(u.getNombre_usuario_modificado(), u.getApellido_usuario_modificado());

        /* LAS CONTRASEÑAS NUNCA SE MUESTRAN */
        if ("CAMBIO_CLAVE".equals(u.getTipo_evento())) {
            return "Cambio de contraseña de " + afectado;
        }

        if ("RESTABLECIMIENTO_CLAVE".equals(u.getTipo_evento())) {
            return "Restablecimiento de contraseña de " + afectado + " por enlace de recuperación";
        }

        return "Modificó \"" + valor(u.getCampo_modificado()) + "\" de " + afectado + ": "
                + valor(u.getValor_anterior()) + " → " + valor(u.getValor_nuevo());
    }

    private static String detalleCaso(Caso c) {
        StringBuilder detalle = new StringBuilder("Caso ").append(valor(c.getNumero_caso()));

        if (c.getEstado_anterior() != null && c.getEstado_nuevo() != null
                && !c.getEstado_anterior().equals(c.getEstado_nuevo())) {
            detalle.append(": ").append(c.getEstado_anterior()).append(" → ").append(c.getEstado_nuevo());
        }
        if (c.getDescripcion() != null && !c.getDescripcion().isBlank()) {
            detalle.append(". ").append(c.getDescripcion());
        }
        return detalle.toString();
    }

    private static String detalleDocumento(Documento d) {
        long kb = d.getTamano_bytes() == null ? 0 : Math.max(1, d.getTamano_bytes() / 1024);
        return valor(d.getNombre_archivo()) + " (" + valor(d.getTipo_archivo()) + ", " + kb + " KB) · Caso "
                + valor(d.getNumero_caso());
    }

    private static List<String> describirFiltros(FiltroAuditoria filtro, int total) {
        List<String> lineas = new ArrayList<>();
        lineas.add("Período: " + (filtro.desde() == null ? "inicio" : filtro.desde().format(FECHA))
                + " al " + (filtro.hasta() == null ? "hoy" : filtro.hasta().format(FECHA)));
        if (filtro.usuario() != null && !filtro.usuario().isBlank()) {
            lineas.add("Búsqueda: " + filtro.usuario().trim());
        }
        if (filtro.evento() != null && !filtro.evento().isBlank()) {
            lineas.add("Evento: " + filtro.evento());
        }
        lineas.add("Registros: " + total + (total >= MAXIMO_FILAS_EXPORTACION ? " (límite de exportación alcanzado)" : ""));
        lineas.add("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        return lineas;
    }

    private static String persona(String nombre, String apellido) {
        String completo = (valor(nombre) + " " + valor(apellido)).trim();
        return completo.isEmpty() ? "Sistema" : completo;
    }

    private static String correo(String correo) {
        return correo == null || correo.isBlank() ? "" : " (" + correo + ")";
    }

    private static String valor(String texto) {
        return texto == null ? "" : texto;
    }
}
