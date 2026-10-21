package com.portal.residencial.casos_service.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.portal.residencial.casos_service.auth.AuthClient;
import com.portal.residencial.casos_service.auth.PersonalResponse;
import com.portal.residencial.casos_service.auth.UsuarioResponse;
import com.portal.residencial.casos_service.bitacora.BitacoraCasoRequest;
import com.portal.residencial.casos_service.bitacora.BitacoraClient;
import com.portal.residencial.casos_service.config.UsuarioAutenticado;
import com.portal.residencial.casos_service.dto.AnalistaResponse;
import com.portal.residencial.casos_service.dto.CasoDetalleResponse;
import com.portal.residencial.casos_service.dto.CasoResponse;
import com.portal.residencial.casos_service.dto.DocumentoResponse;
import com.portal.residencial.casos_service.dto.RegistrarCasoRequest;
import com.portal.residencial.casos_service.dto.SeguimientoResponse;
import com.portal.residencial.casos_service.exception.BusinessException;
import com.portal.residencial.casos_service.model.Caso;
import com.portal.residencial.casos_service.model.EstadoCaso;
import com.portal.residencial.casos_service.model.SeguimientoCaso;
import com.portal.residencial.casos_service.model.TipoArchivo;
import com.portal.residencial.casos_service.model.TipoIncidente;
import com.portal.residencial.casos_service.model.TipoSeguimiento;
import com.portal.residencial.casos_service.notificacion.NotificacionClient;
import com.portal.residencial.casos_service.notificacion.TipoNotificacion;
import com.portal.residencial.casos_service.repository.CasoRepository;
import com.portal.residencial.casos_service.repository.DocumentoRepository;
import com.portal.residencial.casos_service.repository.SeguimientoCasoRepository;
import com.portal.residencial.casos_service.repository.TipoIncidenteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CasoService {

    private static final String ROL_RESIDENTE = "Residente";
    private static final String ROL_ANALISTA = "Analista";
    private static final String ROL_SUPERVISOR = "Supervisor";
    private static final String ROL_ADMINISTRADOR = "Administrador";

    /* USUARIO SISTEMA */
    private static final UsuarioAutenticado SISTEMA =
            new UsuarioAutenticado(null, null, "Sistema", null, "Sistema", null);

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CasoRepository casoRepository;
    private final SeguimientoCasoRepository seguimientoRepository;
    private final TipoIncidenteRepository tipoIncidenteRepository;
    private final DocumentoRepository documentoRepository;
    private final AdjuntoService adjuntoService;
    private final AuthClient authClient;
    private final NotificacionClient notificacionClient;
    private final BitacoraClient bitacoraClient;

    /* REGISTRO DE CASO */

    /* SI UN ADJUNTO NO CUMPLE, EL CASO NO SE REGISTRA */
    @Transactional
    public CasoResponse registrarCaso(RegistrarCasoRequest request, List<MultipartFile> archivos,
            UsuarioAutenticado residente, String ip) {
        TipoIncidente tipo = tipoIncidenteRepository.findById(request.idTipoIncidente())
                .filter(TipoIncidente::isActivo)
                .orElseThrow(() -> new BusinessException("El tipo de incidente seleccionado no existe.",
                        HttpStatus.BAD_REQUEST));

        if (!tipo.getCategoria().getId().equals(request.idCategoria())) {
            throw new BusinessException("El tipo de incidente no pertenece a la categoría seleccionada.",
                    HttpStatus.BAD_REQUEST);
        }

        List<TipoArchivo> tiposArchivo = adjuntoService.validar(archivos, null);

        LocalDateTime ahora = LocalDateTime.now();

        Caso caso = casoRepository.save(Caso.builder()
                .categoria(tipo.getCategoria())
                .tipoIncidente(tipo)
                .descripcion(request.descripcion().trim())
                .estado(EstadoCaso.NUEVO.name())
                .idResidente(residente.id())
                .correoResidente(residente.correo())
                .nombreResidente(residente.nombreCompleto())
                .direccionResidente(obtenerDireccion(residente))
                .fechaRegistro(ahora)
                .fechaActualizacion(ahora)
                .build());

        /* GENERA EL NÚMERO DEL CASO */
        caso.setNumeroCaso(String.format("CASO-%d-%06d", ahora.getYear(), caso.getId()));

        SeguimientoCaso registro = registrarSeguimiento(caso, TipoSeguimiento.REGISTRO, null, EstadoCaso.NUEVO,
                "Caso registrado por el residente.", residente);
        adjuntoService.guardar(caso, registro, archivos, tiposArchivo, residente, ip);

        registrarBitacora(caso, "REGISTRO", null, EstadoCaso.NUEVO,
                "Registro de caso: " + caso.getDescripcion(), residente, ip);

        Map<String, String> datos = datosBase(caso);
        datos.put("fecha", ahora.format(FORMATO_FECHA));
        notificacionClient.enviar(caso.getCorreoResidente(), TipoNotificacion.CASO_CREADO, datos);

        asignarAutomaticamente(caso, residente.token(), ip);

        return aCasoResponse(caso);
    }

    /* DIRECCIÓN DEL RESIDENTE */
    private String obtenerDireccion(UsuarioAutenticado residente) {
        try {
            UsuarioResponse perfil = authClient.obtenerPerfil(residente.token());
            return perfil == null ? null : perfil.direccion();
        } catch (Exception e) {
            log.warn("No se pudo obtener la dirección del residente {}: {}", residente.correo(), e.getMessage());
            return null;
        }
    }

    /* EL RESIDENTE AGREGA INFORMACIÓN */

    /* AGREGA UN COMENTARIO AL CASO Y AVISA AL ANALISTA */
    @Transactional
    public CasoDetalleResponse agregarInformacion(Long idCaso, String comentario, List<MultipartFile> archivos,
            UsuarioAutenticado residente, String ip) {
        Caso caso = buscarCasoVisible(idCaso, residente); /* EL RESIDENTE SOLO VE SUS CASOS */
        EstadoCaso estado = estadoDe(caso);

        if (estado.esFinal()) {
            throw new BusinessException("No se puede agregar información a un caso " +
                    estado.getDescripcion().toLowerCase() + ".", HttpStatus.CONFLICT);
        }

        List<TipoArchivo> tiposArchivo = adjuntoService.validar(archivos, caso.getId());

        caso.setFechaActualizacion(LocalDateTime.now());
        SeguimientoCaso informacion = registrarSeguimiento(caso, TipoSeguimiento.INFORMACION_ADICIONAL,
                estado, estado, comentario.trim(), residente);
        adjuntoService.guardar(caso, informacion, archivos, tiposArchivo, residente, ip);
        registrarBitacora(caso, TipoSeguimiento.INFORMACION_ADICIONAL.name(), estado, estado,
                comentario.trim(), residente, ip);

        if (caso.getCorreoAnalista() != null) {
            notificacionClient.enviar(caso.getCorreoAnalista(), TipoNotificacion.INFORMACION_AGREGADA,
                    datosBase(caso));
        }

        return aDetalle(caso);
    }

    /* ASIGNACIÓN AUTOMÁTICA DE CASOS */

    /* ASIGNA AL ANALISTA CON MENOS CASOS */
    private void asignarAutomaticamente(Caso caso, String token, String ip) {
        List<PersonalResponse> analistas;

        try {
            analistas = authClient.listarPersonal(ROL_ANALISTA, token);
        } catch (Exception e) {
            log.warn("No se pudo obtener la lista de analistas para el caso {}: {}",
                    caso.getNumeroCaso(), e.getMessage());
            return;
        }

        if (analistas.isEmpty()) {
            log.warn("No hay analistas activos: el caso {} queda sin asignar.", caso.getNumeroCaso());
            return;
        }

        Map<Long, Long> carga = analistas.stream()
                .collect(Collectors.toMap(PersonalResponse::id,
                        a -> casoRepository.countByIdAnalistaAndEstadoIn(a.id(), EstadoCaso.ACTIVOS)));

        PersonalResponse elegido = analistas.stream()
                .min((a, b) -> {
                    int porCarga = Long.compare(carga.get(a.id()), carga.get(b.id()));
                    return porCarga != 0 ? porCarga : Long.compare(a.id(), b.id());
                })
                .orElseThrow();

        asignarAnalista(caso, elegido, TipoSeguimiento.ASIGNACION,
                "Caso asignado automáticamente a " + elegido.nombreCompleto() + " por carga de trabajo.",
                SISTEMA, ip);
    }

    /* REASIGNAR CASO */

    @Transactional
    public CasoDetalleResponse reasignar(Long idCaso, Long idAnalista, UsuarioAutenticado supervisor, String ip) {
        Caso caso = buscarCaso(idCaso);
        EstadoCaso estado = estadoDe(caso);

        if (estado.esFinal()) {
            throw new BusinessException("No se puede reasignar un caso " +
                    estado.getDescripcion().toLowerCase() + ".", HttpStatus.BAD_REQUEST);
        }

        if (idAnalista.equals(caso.getIdAnalista())) {
            throw new BusinessException("El caso ya está asignado a ese analista.", HttpStatus.BAD_REQUEST);
        }

        PersonalResponse analista = obtenerAnalistasActivos(supervisor.token()).stream()
                .filter(a -> a.id().equals(idAnalista))
                .findFirst()
                .orElseThrow(() -> new BusinessException("El analista no existe o está inactivo.",
                        HttpStatus.BAD_REQUEST));

        String correoAnterior = caso.getCorreoAnalista();
        TipoSeguimiento tipo = caso.getIdAnalista() == null ? TipoSeguimiento.ASIGNACION : TipoSeguimiento.REASIGNACION;
        String comentario = caso.getIdAnalista() == null
                ? "Caso asignado a " + analista.nombreCompleto() + "."
                : "Caso reasignado de " + caso.getNombreAnalista() + " a " + analista.nombreCompleto() + ".";

        asignarAnalista(caso, analista, tipo, comentario, supervisor, ip);

        if (tipo == TipoSeguimiento.REASIGNACION) {
            notificarReasignacion(caso, correoAnterior, analista.nombreCompleto());
        }

        return aDetalle(caso);
    }

    /* AVISA AL ANALISTA ANTERIOR Y AL RESIDENTE */
    private void notificarReasignacion(Caso caso, String correoAnterior, String nuevoAnalista) {
        /* SIN ID DEL CASO: EL ANALISTA ANTERIOR YA NO PUEDE ABRIRLO, SOLO RECIBE EL CORREO */
        Map<String, String> datosAnterior = datosBase(caso);
        datosAnterior.remove("idCaso");
        notificacionClient.enviar(correoAnterior, TipoNotificacion.CASO_REASIGNADO, datosAnterior);

        Map<String, String> datosResidente = datosBase(caso);
        datosResidente.put("analista", nuevoAnalista);
        notificacionClient.enviar(caso.getCorreoResidente(), TipoNotificacion.CAMBIO_ANALISTA, datosResidente);
    }

    /* CASOS EN CURSO DE UN ANALISTA */
    public long contarCasosEnCurso(Long idAnalista) {
        return casoRepository.countByIdAnalistaAndEstadoIn(idAnalista, EstadoCaso.ACTIVOS);
    }

    /* ANALISTAS CON SUS CASOS EN CURSO */
    public List<AnalistaResponse> listarAnalistas(UsuarioAutenticado supervisor) {
        return obtenerAnalistasActivos(supervisor.token()).stream()
                .map(a -> new AnalistaResponse(a.id(), a.nombre(), a.apellido(), a.correo(),
                        casoRepository.countByIdAnalistaAndEstadoIn(a.id(), EstadoCaso.ACTIVOS)))
                .toList();
    }

    /* LISTA DEL PERSONAL */
    private List<PersonalResponse> obtenerAnalistasActivos(String token) {
        try {
            return authClient.listarPersonal(ROL_ANALISTA, token);
        } catch (Exception e) {
            log.error("No se pudo consultar los analistas en auth-service: {}", e.getMessage());
            throw new BusinessException("No se pudo consultar la lista de analistas. Intente más tarde.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private void asignarAnalista(Caso caso, PersonalResponse analista, TipoSeguimiento tipo,
            String comentario, UsuarioAutenticado actor, String ip) {

        EstadoCaso estadoAnterior = estadoDe(caso);
        EstadoCaso estadoNuevo = estadoAnterior == EstadoCaso.NUEVO ? EstadoCaso.ASIGNADO : estadoAnterior;

        caso.setIdAnalista(analista.id());
        caso.setCorreoAnalista(analista.correo());
        caso.setNombreAnalista(analista.nombreCompleto());
        caso.setEstado(estadoNuevo.name());
        caso.setFechaActualizacion(LocalDateTime.now());

        registrarSeguimiento(caso, tipo, estadoAnterior, estadoNuevo, comentario, actor);
        registrarBitacora(caso, tipo.name(), estadoAnterior, estadoNuevo, comentario, actor, ip);

        Map<String, String> datos = datosBase(caso);
        notificacionClient.enviar(analista.correo(), TipoNotificacion.CASO_ASIGNADO, datos);

        if (estadoNuevo != estadoAnterior) {
            notificarCambioAlResidente(caso);
        }
    }

    /* ANALIZAR Y SOLICITAR AUTORIZACIÓN */

    @Transactional
    public CasoDetalleResponse iniciarAnalisis(Long idCaso, UsuarioAutenticado analista, String ip) {
        Caso caso = buscarCasoDelAnalista(idCaso, analista);
        validarEstado(caso, EstadoCaso.ASIGNADO);

        cambiarEstado(caso, EstadoCaso.EN_ANALISIS, TipoSeguimiento.INICIO_ANALISIS,
                "El analista inició el análisis del caso.", analista, ip);
        notificarCambioAlResidente(caso);

        return aDetalle(caso);
    }

    /* REGISTRA UN AVANCE DEL CASO */
    @Transactional
    public CasoDetalleResponse registrarAvance(Long idCaso, String comentario, List<MultipartFile> archivos,
            UsuarioAutenticado analista, String ip) {
        Caso caso = buscarCasoDelAnalista(idCaso, analista);
        validarEstado(caso, EstadoCaso.EN_ANALISIS, EstadoCaso.EN_PROCESO);
        List<TipoArchivo> tiposArchivo = adjuntoService.validar(archivos, caso.getId());

        EstadoCaso estado = estadoDe(caso);

        caso.setFechaActualizacion(LocalDateTime.now());
        SeguimientoCaso avance = registrarSeguimiento(caso, TipoSeguimiento.AVANCE, estado, estado,
                comentario.trim(), analista);
        adjuntoService.guardar(caso, avance, archivos, tiposArchivo, analista, ip);
        registrarBitacora(caso, TipoSeguimiento.AVANCE.name(), estado, estado, comentario.trim(), analista, ip);

        notificacionClient.enviar(caso.getCorreoResidente(), TipoNotificacion.AVANCE_REGISTRADO, datosBase(caso));

        return aDetalle(caso);
    }

    @Transactional
    public CasoDetalleResponse solicitarAutorizacion(Long idCaso, String comentario, List<MultipartFile> archivos,
            UsuarioAutenticado analista, String ip) {
        Caso caso = buscarCasoDelAnalista(idCaso, analista);
        validarEstado(caso, EstadoCaso.EN_ANALISIS);
        List<TipoArchivo> tiposArchivo = adjuntoService.validar(archivos, caso.getId());

        SeguimientoCaso solicitud = cambiarEstado(caso, EstadoCaso.PENDIENTE_AUTORIZACION,
                TipoSeguimiento.SOLICITUD_AUTORIZACION, comentario.trim(), analista, ip);
        adjuntoService.guardar(caso, solicitud, archivos, tiposArchivo, analista, ip);

        notificarSupervisores(caso, analista.token());
        notificarCambioAlResidente(caso);

        return aDetalle(caso);
    }

    /* AUTORIZAR O RECHAZAR */

    @Transactional
    public CasoDetalleResponse autorizar(Long idCaso, String comentario, UsuarioAutenticado supervisor, String ip) {
        Caso caso = buscarCaso(idCaso);
        validarEstado(caso, EstadoCaso.PENDIENTE_AUTORIZACION);

        String detalle = comentario == null || comentario.isBlank()
                ? "El supervisor autorizó el caso."
                : comentario.trim();

        cambiarEstado(caso, EstadoCaso.EN_PROCESO, TipoSeguimiento.AUTORIZACION, detalle, supervisor, ip);
        notificarDecisionAlAnalista(caso, "autorizó");
        notificarCambioAlResidente(caso);

        return aDetalle(caso);
    }

    @Transactional
    public CasoDetalleResponse rechazar(Long idCaso, String comentario, UsuarioAutenticado supervisor, String ip) {
        Caso caso = buscarCaso(idCaso);
        validarEstado(caso, EstadoCaso.PENDIENTE_AUTORIZACION);

        caso.setFechaCierre(LocalDateTime.now());
        cambiarEstado(caso, EstadoCaso.RECHAZADO, TipoSeguimiento.RECHAZO, comentario.trim(), supervisor, ip);
        notificarDecisionAlAnalista(caso, "rechazó");
        notificarCambioAlResidente(caso);

        return aDetalle(caso);
    }

    /* CERRAR CASO */

    @Transactional
    public CasoDetalleResponse cerrar(Long idCaso, String comentario, List<MultipartFile> archivos,
            UsuarioAutenticado analista, String ip) {
        Caso caso = buscarCasoDelAnalista(idCaso, analista);
        validarEstado(caso, EstadoCaso.EN_PROCESO);
        List<TipoArchivo> tiposArchivo = adjuntoService.validar(archivos, caso.getId());

        LocalDateTime ahora = LocalDateTime.now();
        caso.setFechaCierre(ahora);
        SeguimientoCaso cierre = cambiarEstado(caso, EstadoCaso.FINALIZADO, TipoSeguimiento.CIERRE,
                comentario.trim(), analista, ip);
        adjuntoService.guardar(caso, cierre, archivos, tiposArchivo, analista, ip);

        Map<String, String> datos = datosBase(caso);
        datos.put("fecha", ahora.format(FORMATO_FECHA));
        notificacionClient.enviar(caso.getCorreoResidente(), TipoNotificacion.CASO_FINALIZADO, datos);

        return aDetalle(caso);
    }

    /* CONSULTAS */

    public List<CasoResponse> listarMisCasos(UsuarioAutenticado residente) {
        return casoRepository.findByIdResidenteOrderByFechaRegistroDesc(residente.id()).stream()
                .map(this::aCasoResponse)
                .toList();
    }

    /* CASOS QUE PUEDE VER EL USUARIO */
    public List<CasoResponse> listarBandeja(UsuarioAutenticado usuario) {
        List<Caso> casos = usuario.esRol(ROL_ANALISTA)
                ? casoRepository.findByIdAnalistaOrderByFechaActualizacionDesc(usuario.id())
                : casoRepository.findAllByOrderByFechaActualizacionDesc();

        return casos.stream().map(this::aCasoResponse).toList();
    }

    public CasoDetalleResponse obtenerDetalle(Long idCaso, UsuarioAutenticado usuario) {
        return aDetalle(buscarCasoVisible(idCaso, usuario));
    }

    /* BUSCA EL CASO SI EL USUARIO PUEDE VERLO */
    public Caso buscarCasoVisible(Long idCaso, UsuarioAutenticado usuario) {
        Caso caso = buscarCaso(idCaso);

        boolean puedeVer = switch (usuario.rol()) {
            case ROL_RESIDENTE -> Objects.equals(caso.getIdResidente(), usuario.id());
            case ROL_ANALISTA -> Objects.equals(caso.getIdAnalista(), usuario.id());
            case ROL_SUPERVISOR, ROL_ADMINISTRADOR -> true;
            default -> false;
        };

        /* NO SE MUESTRAN CASOS AJENOS */
        if (!puedeVer) {
            throw new BusinessException("Caso no encontrado.", HttpStatus.NOT_FOUND);
        }

        return caso;
    }

    /* UTILIDADES */

    private Caso buscarCaso(Long idCaso) {
        return casoRepository.findById(idCaso)
                .orElseThrow(() -> new BusinessException("Caso no encontrado.", HttpStatus.NOT_FOUND));
    }

    /* SOLO EL ANALISTA ASIGNADO PUEDE TRABAJAR EL CASO */
    private Caso buscarCasoDelAnalista(Long idCaso, UsuarioAutenticado analista) {
        Caso caso = buscarCaso(idCaso);

        if (!Objects.equals(caso.getIdAnalista(), analista.id())) {
            throw new BusinessException("Este caso no está asignado a usted.", HttpStatus.FORBIDDEN);
        }

        return caso;
    }

    /* CAMBIA EL ESTADO DEL CASO */
    static EstadoCaso estadoDe(Caso caso) {
        return EstadoCaso.valueOf(caso.getEstado());
    }

    private void validarEstado(Caso caso, EstadoCaso... permitidos) {
        EstadoCaso actual = estadoDe(caso);

        if (!Set.of(permitidos).contains(actual)) {
            throw new BusinessException("Acción no permitida: el caso está en estado \"" +
                    actual.getDescripcion() + "\".", HttpStatus.CONFLICT);
        }
    }

    private SeguimientoCaso cambiarEstado(Caso caso, EstadoCaso estadoNuevo, TipoSeguimiento tipo,
            String comentario, UsuarioAutenticado actor, String ip) {
        EstadoCaso estadoAnterior = estadoDe(caso);

        caso.setEstado(estadoNuevo.name());
        caso.setFechaActualizacion(LocalDateTime.now());

        SeguimientoCaso seguimiento = registrarSeguimiento(caso, tipo, estadoAnterior, estadoNuevo, comentario, actor);
        registrarBitacora(caso, tipo.name(), estadoAnterior, estadoNuevo, comentario, actor, ip);

        return seguimiento;
    }

    private SeguimientoCaso registrarSeguimiento(Caso caso, TipoSeguimiento tipo, EstadoCaso estadoAnterior,
            EstadoCaso estadoNuevo, String comentario, UsuarioAutenticado actor) {
        return seguimientoRepository.save(SeguimientoCaso.builder()
                .caso(caso)
                .tipo(tipo.name())
                .estadoAnterior(estadoAnterior == null ? null : estadoAnterior.name())
                .estadoNuevo(estadoNuevo == null ? null : estadoNuevo.name())
                .comentario(comentario)
                .idUsuario(actor.id())
                .nombreUsuario(actor.nombreCompleto())
                .rolUsuario(actor.rol())
                .fecha(LocalDateTime.now())
                .build());
    }

    private void registrarBitacora(Caso caso, String tipoEvento, EstadoCaso estadoAnterior,
            EstadoCaso estadoNuevo, String descripcion, UsuarioAutenticado actor, String ip) {
        bitacoraClient.registrar(new BitacoraCasoRequest(
                caso.getId(),
                caso.getNumeroCaso(),
                tipoEvento,
                estadoAnterior == null ? null : estadoAnterior.name(),
                estadoNuevo == null ? null : estadoNuevo.name(),
                descripcion,
                ip,
                actor.id(),
                actor.nombre(),
                actor.apellido(),
                actor.rol()));
    }

    /* NOTIFICACIONES */

    /* DATOS DE LA NOTIFICACIÓN */
    private Map<String, String> datosBase(Caso caso) {
        Map<String, String> datos = new HashMap<>();
        datos.put("idCaso", String.valueOf(caso.getId()));
        datos.put("numeroCaso", caso.getNumeroCaso());
        datos.put("titulo", caso.getTipoIncidente().getNombre());
        datos.put("categoria", caso.getCategoria().getNombre());
        return datos;
    }

    private void notificarCambioAlResidente(Caso caso) {
        Map<String, String> datos = datosBase(caso);
        datos.put("nuevoEstado", estadoDe(caso).getDescripcion());
        notificacionClient.enviar(caso.getCorreoResidente(), TipoNotificacion.CASO_ACTUALIZADO, datos);
    }

    private void notificarDecisionAlAnalista(Caso caso, String decision) {
        Map<String, String> datos = datosBase(caso);
        datos.put("decision", decision);
        notificacionClient.enviar(caso.getCorreoAnalista(), TipoNotificacion.CASO_AUTORIZADO_RECHAZADO, datos);
    }

    private void notificarSupervisores(Caso caso, String token) {
        try {
            Map<String, String> datos = datosBase(caso);
            authClient.listarPersonal(ROL_SUPERVISOR, token).forEach(supervisor ->
                    notificacionClient.enviar(supervisor.correo(), TipoNotificacion.TRASLADO_AUTORIZACION, datos));
        } catch (Exception e) {
            log.warn("No se pudo notificar a los supervisores del caso {}: {}",
                    caso.getNumeroCaso(), e.getMessage());
        }
    }

    /* CONVERSIÓN A RESPUESTAS */

    private CasoDetalleResponse aDetalle(Caso caso) {
        /* ADJUNTOS DE CADA MOVIMIENTO */
        Map<Long, List<DocumentoResponse>> documentosPorSeguimiento = documentoRepository
                .findByCaso_IdOrderByFechaCargaAscIdAsc(caso.getId()).stream()
                .filter(documento -> documento.getSeguimiento() != null)
                .collect(Collectors.groupingBy(
                        documento -> documento.getSeguimiento().getId(),
                        Collectors.mapping(DocumentoResponse::desde, Collectors.toList())));

        List<SeguimientoResponse> seguimientos = seguimientoRepository
                .findByCaso_IdOrderByFechaAscIdAsc(caso.getId()).stream()
                .map(seguimiento -> aSeguimientoResponse(seguimiento,
                        documentosPorSeguimiento.getOrDefault(seguimiento.getId(), List.of())))
                .toList();

        return new CasoDetalleResponse(aCasoResponse(caso), seguimientos);
    }

    private CasoResponse aCasoResponse(Caso caso) {
        return new CasoResponse(
                caso.getId(),
                caso.getNumeroCaso(),
                caso.getCategoria().getNombre(),
                caso.getTipoIncidente().getNombre(),
                caso.getDescripcion(),
                caso.getEstado(),
                estadoDe(caso).getDescripcion(),
                caso.getNombreResidente(),
                caso.getCorreoResidente(),
                caso.getDireccionResidente(),
                caso.getIdAnalista(),
                caso.getNombreAnalista(),
                caso.getCorreoAnalista(),
                caso.getFechaRegistro(),
                caso.getFechaActualizacion(),
                caso.getFechaCierre());
    }

    private SeguimientoResponse aSeguimientoResponse(SeguimientoCaso seguimiento, List<DocumentoResponse> documentos) {
        return new SeguimientoResponse(
                seguimiento.getId(),
                seguimiento.getTipo(),
                seguimiento.getEstadoAnterior(),
                seguimiento.getEstadoNuevo(),
                seguimiento.getComentario(),
                seguimiento.getNombreUsuario(),
                seguimiento.getRolUsuario(),
                seguimiento.getFecha(),
                documentos);
    }
}
