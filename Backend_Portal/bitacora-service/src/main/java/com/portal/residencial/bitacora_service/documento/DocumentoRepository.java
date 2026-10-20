package com.portal.residencial.bitacora_service.documento;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface DocumentoRepository extends JpaRepository<Documento, Long>, JpaSpecificationExecutor<Documento> {

    /* LISTA DE EVENTOS PARA EL FILTRO DEL REPORTE */
    @Query("select distinct b.tipo_evento from Documento b where b.tipo_evento is not null order by b.tipo_evento")
    List<String> listarTiposEvento();
}
