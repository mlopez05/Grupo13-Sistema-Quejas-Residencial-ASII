package com.portal.residencial.casos_service.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.portal.residencial.casos_service.model.Categoria;
import com.portal.residencial.casos_service.model.TipoIncidente;
import com.portal.residencial.casos_service.repository.CategoriaRepository;
import com.portal.residencial.casos_service.repository.TipoIncidenteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/* CARGA EL CATÁLOGO INICIAL SI ESTÁ VACÍO */
@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogoInicial implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final TipoIncidenteRepository tipoIncidenteRepository;

    private static final Map<String, List<String>> CATALOGO = new LinkedHashMap<>();

    static {
        CATALOGO.put("Convivencia/Reglamento", List.of(
                "Ruido excesivo",
                "Mascotas",
                "Uso indebido de áreas comunes",
                "Manejo inadecuado de basura",
                "Estacionamiento indebido"));
        CATALOGO.put("Seguridad", List.of(
                "Robo o hurto",
                "Persona o vehículo sospechoso",
                "Vandalismo",
                "Falla en control de acceso",
                "Iluminación deficiente"));
        CATALOGO.put("Mantenimiento/Infraestructura", List.of(
                "Fuga de agua",
                "Falla eléctrica",
                "Daño en áreas comunes",
                "Jardinería y áreas verdes",
                "Drenajes obstruidos"));
        CATALOGO.put("Administrativo/Construcción", List.of(
                "Construcción o remodelación sin permiso",
                "Molestias por obra en curso",
                "Cobros y cuotas",
                "Atención administrativa",
                "Sugerencia de mejora"));
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoriaRepository.count() > 0) {
            return;
        }

        CATALOGO.forEach((nombreCategoria, tipos) -> {
            Categoria categoria = categoriaRepository.save(
                    Categoria.builder().nombre(nombreCategoria).activo(true).build());

            tipos.forEach(nombreTipo -> tipoIncidenteRepository.save(
                    TipoIncidente.builder().nombre(nombreTipo).activo(true).categoria(categoria).build()));
        });

        log.info("Catálogo de categorías y tipos de incidente cargado ({} categorías).", CATALOGO.size());
    }
}
