package com.portal.residencial.casos_service.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portal.residencial.casos_service.dto.CategoriaResponse;
import com.portal.residencial.casos_service.dto.TipoIncidenteResponse;
import com.portal.residencial.casos_service.dto.catalogo.CategoriaAdminResponse;
import com.portal.residencial.casos_service.dto.catalogo.CategoriaAdminResponse.TipoAdmin;
import com.portal.residencial.casos_service.dto.catalogo.CategoriaRequest;
import com.portal.residencial.casos_service.dto.catalogo.TipoIncidenteRequest;
import com.portal.residencial.casos_service.exception.BusinessException;
import com.portal.residencial.casos_service.model.Categoria;
import com.portal.residencial.casos_service.model.TipoIncidente;
import com.portal.residencial.casos_service.repository.CasoRepository;
import com.portal.residencial.casos_service.repository.CategoriaRepository;
import com.portal.residencial.casos_service.repository.TipoIncidenteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final CategoriaRepository categoriaRepository;
    private final TipoIncidenteRepository tipoIncidenteRepository;
    private final CasoRepository casoRepository;

    /* CATEGORIAS Y TIPOS ACTIVOS PARA EL FORMULARIO DE NUEVO CASO */
    public List<CategoriaResponse> listarCatalogo() {
        Map<Long, List<TipoIncidenteResponse>> tiposPorCategoria =
                tipoIncidenteRepository.findByActivoTrueOrderByNombreAsc().stream()
                        .collect(Collectors.groupingBy(
                                (TipoIncidente tipo) -> tipo.getCategoria().getId(),
                                Collectors.mapping(
                                        tipo -> new TipoIncidenteResponse(tipo.getId(), tipo.getNombre()),
                                        Collectors.toList())));

        return categoriaRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(categoria -> new CategoriaResponse(
                        categoria.getId(),
                        categoria.getNombre(),
                        tiposPorCategoria.getOrDefault(categoria.getId(), List.of())))
                .toList();
    }

    /* ADMINISTRACION DEL CATALOGO: CADA CONDOMINIO ARMA SUS PROPIAS CATEGORIAS Y TIPOS */
    public List<CategoriaAdminResponse> listarCatalogoAdmin() {
        Map<Long, Long> casosPorCategoria = conteos(casoRepository.contarPorCategoria());
        Map<Long, Long> casosPorTipo = conteos(casoRepository.contarPorTipoIncidente());

        Map<Long, List<TipoAdmin>> tiposPorCategoria = tipoIncidenteRepository.findAllByOrderByNombreAsc().stream()
                .collect(Collectors.groupingBy(
                        (TipoIncidente tipo) -> tipo.getCategoria().getId(),
                        Collectors.mapping(tipo -> new TipoAdmin(tipo.getId(), tipo.getNombre(), tipo.isActivo(),
                                        casosPorTipo.getOrDefault(tipo.getId(), 0L)),
                                Collectors.toList())));

        return categoriaRepository.findAllByOrderByNombreAsc().stream()
                .map(categoria -> new CategoriaAdminResponse(
                        categoria.getId(),
                        categoria.getNombre(),
                        categoria.isActivo(),
                        casosPorCategoria.getOrDefault(categoria.getId(), 0L),
                        tiposPorCategoria.getOrDefault(categoria.getId(), List.of())))
                .toList();
    }

    @Transactional
    public void crearCategoria(CategoriaRequest request) {
        String nombre = request.nombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe una categoría con el nombre \"" + nombre + "\".", HttpStatus.CONFLICT);
        }

        categoriaRepository.save(Categoria.builder().nombre(nombre).activo(request.activo()).build());
    }

    @Transactional
    public void actualizarCategoria(Long id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Categoría no encontrada.", HttpStatus.NOT_FOUND));
        String nombre = request.nombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new BusinessException("Ya existe una categoría con el nombre \"" + nombre + "\".", HttpStatus.CONFLICT);
        }

        categoria.setNombre(nombre);
        categoria.setActivo(request.activo());
    }

    @Transactional
    public void crearTipo(TipoIncidenteRequest request) {
        Categoria categoria = buscarCategoria(request.idCategoria());
        String nombre = request.nombre().trim();

        if (tipoIncidenteRepository.existsByCategoria_IdAndNombreIgnoreCase(categoria.getId(), nombre)) {
            throw new BusinessException("La categoría ya tiene un tipo llamado \"" + nombre + "\".", HttpStatus.CONFLICT);
        }

        tipoIncidenteRepository.save(TipoIncidente.builder()
                .nombre(nombre).activo(request.activo()).categoria(categoria).build());
    }

    @Transactional
    public void actualizarTipo(Long id, TipoIncidenteRequest request) {
        TipoIncidente tipo = tipoIncidenteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tipo de incidente no encontrado.", HttpStatus.NOT_FOUND));
        Categoria categoria = buscarCategoria(request.idCategoria());
        String nombre = request.nombre().trim();

        if (tipoIncidenteRepository.existsByCategoria_IdAndNombreIgnoreCaseAndIdNot(categoria.getId(), nombre, id)) {
            throw new BusinessException("La categoría ya tiene un tipo llamado \"" + nombre + "\".", HttpStatus.CONFLICT);
        }

        /* UN TIPO QUE YA TIENE CASOS NO SE PUEDE MOVER A OTRA CATEGORIA */
        if (!tipo.getCategoria().getId().equals(categoria.getId()) && casoRepository.existsByTipoIncidente_Id(id)) {
            throw new BusinessException("El tipo ya tiene casos registrados y no se puede mover a otra categoría.",
                    HttpStatus.CONFLICT);
        }

        tipo.setNombre(nombre);
        tipo.setActivo(request.activo());
        tipo.setCategoria(categoria);
    }

    /* SOLO SE ELIMINA SI NINGUN CASO LA USA; SI NO, SE DEBE DESACTIVAR */
    @Transactional
    public void eliminarCategoria(Long id) {
        Categoria categoria = buscarCategoria(id);
        List<TipoIncidente> tipos = tipoIncidenteRepository.findByCategoria_Id(id);

        boolean enUso = casoRepository.existsByCategoria_Id(id)
                || tipos.stream().anyMatch(tipo -> casoRepository.existsByTipoIncidente_Id(tipo.getId()));

        if (enUso) {
            throw new BusinessException("La categoría tiene casos registrados. Desactívala en lugar de eliminarla.",
                    HttpStatus.CONFLICT);
        }

        tipoIncidenteRepository.deleteAll(tipos);
        categoriaRepository.delete(categoria);
    }

    /* SOLO SE ELIMINA SI NINGUN CASO LO USA; SI NO, SE DEBE DESACTIVAR */
    @Transactional
    public void eliminarTipo(Long id) {
        TipoIncidente tipo = tipoIncidenteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tipo de incidente no encontrado.", HttpStatus.NOT_FOUND));

        if (casoRepository.existsByTipoIncidente_Id(id)) {
            throw new BusinessException("El tipo tiene casos registrados. Desactívalo en lugar de eliminarlo.",
                    HttpStatus.CONFLICT);
        }

        tipoIncidenteRepository.delete(tipo);
    }

    private static Map<Long, Long> conteos(List<Object[]> filas) {
        return filas.stream().collect(Collectors.toMap(fila -> (Long) fila[0], fila -> (Long) fila[1]));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Categoría no encontrada.", HttpStatus.NOT_FOUND));
    }
}
