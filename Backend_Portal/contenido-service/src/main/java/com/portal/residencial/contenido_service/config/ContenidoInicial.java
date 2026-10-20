package com.portal.residencial.contenido_service.config;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.portal.residencial.contenido_service.model.Aviso;
import com.portal.residencial.contenido_service.model.CategoriaAviso;
import com.portal.residencial.contenido_service.model.ConfiguracionPortal;
import com.portal.residencial.contenido_service.model.Servicio;
import com.portal.residencial.contenido_service.repository.AvisoRepository;
import com.portal.residencial.contenido_service.repository.ConfiguracionPortalRepository;
import com.portal.residencial.contenido_service.repository.ServicioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/* CARGA EL CONTENIDO INICIAL DEL PORTAL */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContenidoInicial implements CommandLineRunner {

    private static final String SISTEMA = "Sistema";

    private final ConfiguracionPortalRepository configuracionRepository;
    private final AvisoRepository avisoRepository;
    private final ServicioRepository servicioRepository;

    @Override
    @Transactional
    public void run(String... args) {
        LocalDateTime ahora = LocalDateTime.now();

        if (!configuracionRepository.existsById(ConfiguracionPortal.ID_UNICO)) {
            configuracionRepository.save(ConfiguracionPortal.builder()
                    .id(ConfiguracionPortal.ID_UNICO)
                    .nombreResidencial("Portal Residencial")
                    .lema("Comunidad y gestión")
                    .horarioAtencion("Lunes a viernes, 8:00 AM - 5:00 PM")
                    .tituloBienvenida("Bienvenido al Portal Residencial")
                    .mensajeBienvenida("Un espacio digital pensado para mantener nuestra comunidad informada, "
                            + "organizada y conectada con la administración.")
                    .descripcionComunidad("Este portal busca facilitar la comunicación entre residentes y "
                            + "administración, proporcionando herramientas digitales que permitan mejorar la atención, "
                            + "organización y convivencia. Nuestro objetivo es que cada residente pueda acceder "
                            + "fácilmente a información importante y mantener una comunicación efectiva con la administración.")
                    .mision("Facilitar la comunicación entre residentes y administración mediante herramientas digitales "
                            + "que permitan atender solicitudes, reportes e incidencias de manera organizada, "
                            + "transparente y eficiente.")
                    .vision("Ser una comunidad residencial moderna, participativa y conectada, utilizando la tecnología "
                            + "como herramienta para mejorar continuamente la convivencia y la calidad de los servicios.")
                    .fechaActualizacion(ahora)
                    .actualizadoPor(SISTEMA)
                    .build());
            log.info("Configuración inicial del portal cargada.");
        }

        if (avisoRepository.count() == 0) {
            avisoRepository.save(aviso("Mantenimiento de áreas comunes",
                    "Se realizarán trabajos preventivos en las áreas comunes para mantener nuestros espacios en "
                            + "óptimas condiciones.", CategoriaAviso.MANTENIMIENTO, ahora));
            avisoRepository.save(aviso("Información de servicios",
                    "Consulta con anticipación cualquier actualización relacionada con los servicios generales "
                            + "del residencial.", CategoriaAviso.IMPORTANTE, ahora));
            avisoRepository.save(aviso("Recomendaciones de convivencia",
                    "Una buena convivencia se construye con respeto, comunicación y participación de todos los "
                            + "residentes.", CategoriaAviso.COMUNIDAD, ahora));
        }

        if (servicioRepository.count() == 0) {
            servicioRepository.save(servicio("Seguridad", "Control de acceso y vigilancia de las instalaciones.",
                    "24 horas", 1, ahora));
            servicioRepository.save(servicio("Mantenimiento de áreas comunes",
                    "Limpieza, jardinería y reparaciones de las áreas compartidas.", "Lunes a sábado", 2, ahora));
            servicioRepository.save(servicio("Atención administrativa",
                    "Consultas, pagos de cuotas y gestiones con la administración.", "Lunes a viernes, 8:00 AM - 5:00 PM",
                    3, ahora));
        }
    }

    private static Aviso aviso(String titulo, String contenido, CategoriaAviso categoria, LocalDateTime ahora) {
        return Aviso.builder().titulo(titulo).contenido(contenido).categoria(categoria.name())
                .publicado(true).fechaPublicacion(LocalDate.now())
                .fechaCreacion(ahora).fechaActualizacion(ahora).actualizadoPor(SISTEMA).build();
    }

    private static Servicio servicio(String nombre, String descripcion, String horario, int orden, LocalDateTime ahora) {
        return Servicio.builder().nombre(nombre).descripcion(descripcion).horario(horario).orden(orden)
                .activo(true).fechaActualizacion(ahora).actualizadoPor(SISTEMA).build();
    }
}
