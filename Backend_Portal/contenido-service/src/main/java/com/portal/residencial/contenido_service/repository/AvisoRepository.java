package com.portal.residencial.contenido_service.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.portal.residencial.contenido_service.model.Aviso;

public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    /* AVISOS VIGENTES */
    @Query("select a from Aviso a where a.publicado = true and a.fechaPublicacion <= :hoy " +
           "and (a.fechaVencimiento is null or a.fechaVencimiento >= :hoy) " +
           "order by a.fechaPublicacion desc, a.id desc")
    List<Aviso> listarVigentes(@Param("hoy") LocalDate hoy, Pageable pagina);

    List<Aviso> findAllByOrderByFechaPublicacionDescIdDesc();
}
