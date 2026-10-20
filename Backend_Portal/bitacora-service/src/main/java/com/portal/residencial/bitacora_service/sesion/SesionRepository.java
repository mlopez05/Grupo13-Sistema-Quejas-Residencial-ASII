package com.portal.residencial.bitacora_service.sesion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface SesionRepository extends JpaRepository<Sesion, Long>, JpaSpecificationExecutor<Sesion> {

    /* LISTA DE EVENTOS PARA EL FILTRO DEL REPORTE */
    @Query("select distinct b.tipo_evento from Sesion b where b.tipo_evento is not null order by b.tipo_evento")
    List<String> listarTiposEvento();
}
