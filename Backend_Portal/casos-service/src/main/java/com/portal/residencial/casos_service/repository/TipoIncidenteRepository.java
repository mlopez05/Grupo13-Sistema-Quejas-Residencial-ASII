package com.portal.residencial.casos_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portal.residencial.casos_service.model.TipoIncidente;

public interface TipoIncidenteRepository extends JpaRepository<TipoIncidente, Long> {

    List<TipoIncidente> findByActivoTrueOrderByNombreAsc();

    List<TipoIncidente> findAllByOrderByNombreAsc();

    boolean existsByCategoria_IdAndNombreIgnoreCase(Long idCategoria, String nombre);

    boolean existsByCategoria_IdAndNombreIgnoreCaseAndIdNot(Long idCategoria, String nombre, Long id);

    List<TipoIncidente> findByCategoria_Id(Long idCategoria);
}
