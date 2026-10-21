package com.portal.residencial.contenido_service.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portal.residencial.contenido_service.dto.AvisoDtos.AvisoRequest;
import com.portal.residencial.contenido_service.dto.AvisoDtos.AvisoResponse;
import com.portal.residencial.contenido_service.dto.ConfiguracionPortalDto;
import com.portal.residencial.contenido_service.dto.ServicioDtos.ServicioRequest;
import com.portal.residencial.contenido_service.dto.ServicioDtos.ServicioResponse;
import com.portal.residencial.contenido_service.exception.BusinessException;
import com.portal.residencial.contenido_service.model.Aviso;
import com.portal.residencial.contenido_service.model.CategoriaAviso;
import com.portal.residencial.contenido_service.model.ConfiguracionPortal;
import com.portal.residencial.contenido_service.model.Servicio;
import com.portal.residencial.contenido_service.repository.AvisoRepository;
import com.portal.residencial.contenido_service.repository.ConfiguracionPortalRepository;
import com.portal.residencial.contenido_service.repository.ServicioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContenidoService {

    private static final int MAXIMO_AVISOS_PUBLICOS = 50;

    private final ConfiguracionPortalRepository configuracionRepository;
    private final AvisoRepository avisoRepository;
    private final ServicioRepository servicioRepository;

    /* DATOS DEL RESIDENCIAL */

    public ConfiguracionPortalDto obtenerConfiguracion() {
        return aDto(configuracion());
    }

    @Transactional
    public ConfiguracionPortalDto actualizarConfiguracion(ConfiguracionPortalDto dto, String usuario) {
        ConfiguracionPortal c = configuracion();

        c.setNombreResidencial(texto(dto.nombreResidencial()));
        c.setLema(texto(dto.lema()));
        c.setRazonSocial(texto(dto.razonSocial()));
        c.setNit(dto.nit() == null ? null : texto(dto.nit().toUpperCase()));
        c.setDireccion(texto(dto.direccion()));
        c.setTelefono(texto(dto.telefono()));
        c.setCorreoContacto(texto(dto.correoContacto()));
        c.setHorarioAtencion(texto(dto.horarioAtencion()));
        c.setTituloBienvenida(texto(dto.tituloBienvenida()));
        c.setMensajeBienvenida(texto(dto.mensajeBienvenida()));
        c.setDescripcionComunidad(texto(dto.descripcionComunidad()));
        c.setMision(texto(dto.mision()));
        c.setVision(texto(dto.vision()));
        c.setFechaActualizacion(LocalDateTime.now());
        c.setActualizadoPor(usuario);

        return aDto(c);
    }

    private ConfiguracionPortal configuracion() {
        return configuracionRepository.findById(ConfiguracionPortal.ID_UNICO)
                .orElseThrow(() -> new BusinessException("La configuración del portal no ha sido inicializada.",
                        HttpStatus.SERVICE_UNAVAILABLE));
    }

    /* AVISOS */

    public List<AvisoResponse> listarAvisosVigentes(Integer limite) {
        int cantidad = limite == null || limite < 1 ? MAXIMO_AVISOS_PUBLICOS : Math.min(limite, MAXIMO_AVISOS_PUBLICOS);

        return avisoRepository.listarVigentes(LocalDate.now(), PageRequest.of(0, cantidad)).stream()
                .map(this::aResponse)
                .toList();
    }

    public List<AvisoResponse> listarAvisos() {
        return avisoRepository.findAllByOrderByFechaPublicacionDescIdDesc().stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional
    public AvisoResponse crearAviso(AvisoRequest request, String usuario) {
        Aviso aviso = new Aviso();
        aviso.setFechaCreacion(LocalDateTime.now());
        aplicar(aviso, request, usuario);
        return aResponse(avisoRepository.save(aviso));
    }

    @Transactional
    public AvisoResponse actualizarAviso(Long id, AvisoRequest request, String usuario) {
        Aviso aviso = avisoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Aviso no encontrado.", HttpStatus.NOT_FOUND));
        aplicar(aviso, request, usuario);
        return aResponse(aviso);
    }

    @Transactional
    public void eliminarAviso(Long id) {
        if (!avisoRepository.existsById(id)) {
            throw new BusinessException("Aviso no encontrado.", HttpStatus.NOT_FOUND);
        }
        avisoRepository.deleteById(id);
    }

    private void aplicar(Aviso aviso, AvisoRequest request, String usuario) {
        CategoriaAviso categoria = Arrays.stream(CategoriaAviso.values())
                .filter(c -> c.name().equals(request.categoria()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Categoría de aviso no válida.", HttpStatus.BAD_REQUEST));

        if (request.fechaVencimiento() != null && request.fechaVencimiento().isBefore(request.fechaPublicacion())) {
            throw new BusinessException("La fecha de vencimiento no puede ser anterior a la de publicación.",
                    HttpStatus.BAD_REQUEST);
        }

        aviso.setTitulo(request.titulo().trim());
        aviso.setContenido(request.contenido().trim());
        aviso.setCategoria(categoria.name());
        aviso.setPublicado(request.publicado());
        aviso.setFechaPublicacion(request.fechaPublicacion());
        aviso.setFechaVencimiento(request.fechaVencimiento());
        aviso.setFechaActualizacion(LocalDateTime.now());
        aviso.setActualizadoPor(usuario);
    }

    private AvisoResponse aResponse(Aviso a) {
        LocalDate hoy = LocalDate.now();
        boolean vigente = a.isPublicado() && !a.getFechaPublicacion().isAfter(hoy)
                && (a.getFechaVencimiento() == null || !a.getFechaVencimiento().isBefore(hoy));

        return new AvisoResponse(a.getId(), a.getTitulo(), a.getContenido(), a.getCategoria(),
                CategoriaAviso.valueOf(a.getCategoria()).getEtiqueta(), a.isPublicado(),
                a.getFechaPublicacion(), a.getFechaVencimiento(), vigente,
                a.getFechaActualizacion(), a.getActualizadoPor());
    }

    /* SERVICIOS */

    public List<ServicioResponse> listarServiciosActivos() {
        return servicioRepository.findByActivoTrueOrderByOrdenAscNombreAsc().stream().map(this::aResponse).toList();
    }

    public List<ServicioResponse> listarServicios() {
        return servicioRepository.findAllByOrderByOrdenAscNombreAsc().stream().map(this::aResponse).toList();
    }

    @Transactional
    public ServicioResponse crearServicio(ServicioRequest request, String usuario) {
        Servicio servicio = new Servicio();
        aplicar(servicio, request, usuario);
        return aResponse(servicioRepository.save(servicio));
    }

    @Transactional
    public ServicioResponse actualizarServicio(Long id, ServicioRequest request, String usuario) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Servicio no encontrado.", HttpStatus.NOT_FOUND));
        aplicar(servicio, request, usuario);
        return aResponse(servicio);
    }

    @Transactional
    public void eliminarServicio(Long id) {
        if (!servicioRepository.existsById(id)) {
            throw new BusinessException("Servicio no encontrado.", HttpStatus.NOT_FOUND);
        }
        servicioRepository.deleteById(id);
    }

    private void aplicar(Servicio servicio, ServicioRequest request, String usuario) {
        servicio.setNombre(request.nombre().trim());
        servicio.setDescripcion(request.descripcion().trim());
        servicio.setHorario(texto(request.horario()));
        servicio.setOrden(request.orden());
        servicio.setActivo(request.activo());
        servicio.setFechaActualizacion(LocalDateTime.now());
        servicio.setActualizadoPor(usuario);
    }

    private ServicioResponse aResponse(Servicio s) {
        return new ServicioResponse(s.getId(), s.getNombre(), s.getDescripcion(), s.getHorario(), s.getOrden(),
                s.isActivo());
    }

    /* UTILIDADES */

    private static ConfiguracionPortalDto aDto(ConfiguracionPortal c) {
        return new ConfiguracionPortalDto(c.getNombreResidencial(), c.getLema(), c.getRazonSocial(), c.getNit(),
                c.getDireccion(), c.getTelefono(), c.getCorreoContacto(), c.getHorarioAtencion(),
                c.getTituloBienvenida(), c.getMensajeBienvenida(), c.getDescripcionComunidad(),
                c.getMision(), c.getVision());
    }

    /* LIMPIA EL TEXTO */
    private static String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
