package com.portal.residencial.casos_service.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.portal.residencial.casos_service.model.Caso;

public interface CasoRepository extends JpaRepository<Caso, Long>, JpaSpecificationExecutor<Caso> {

    List<Caso> findByIdResidenteOrderByFechaRegistroDesc(Long idResidente);

    List<Caso> findByIdAnalistaOrderByFechaActualizacionDesc(Long idAnalista);

    List<Caso> findAllByOrderByFechaActualizacionDesc();

    long countByIdAnalistaAndEstadoIn(Long idAnalista, Collection<String> estados);

    boolean existsByCategoria_Id(Long idCategoria);

    boolean existsByTipoIncidente_Id(Long idTipoIncidente);

    /* CUANTOS CASOS HAY POR CATEGORIA: [ID, CANTIDAD] */
    @Query("select c.categoria.id, count(c) from Caso c where c.categoria is not null group by c.categoria.id")
    List<Object[]> contarPorCategoria();

    /* CUANTOS CASOS HAY POR TIPO DE INCIDENTE: [ID, CANTIDAD] */
    @Query("select c.tipoIncidente.id, count(c) from Caso c where c.tipoIncidente is not null group by c.tipoIncidente.id")
    List<Object[]> contarPorTipoIncidente();
}
