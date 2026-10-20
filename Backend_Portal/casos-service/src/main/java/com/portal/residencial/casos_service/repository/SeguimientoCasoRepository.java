package com.portal.residencial.casos_service.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portal.residencial.casos_service.model.SeguimientoCaso;

public interface SeguimientoCasoRepository extends JpaRepository<SeguimientoCaso, Long> {

    List<SeguimientoCaso> findByCaso_IdOrderByFechaAscIdAsc(Long idCaso);

    /* HISTORIAL DE VARIOS CASOS */
    List<SeguimientoCaso> findByCaso_IdInOrderByFechaAscIdAsc(Collection<Long> idsCaso);
}
