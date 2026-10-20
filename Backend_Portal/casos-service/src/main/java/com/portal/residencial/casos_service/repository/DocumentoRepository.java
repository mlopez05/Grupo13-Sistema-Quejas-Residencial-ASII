package com.portal.residencial.casos_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.portal.residencial.casos_service.model.Documento;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByCaso_IdOrderByFechaCargaAscIdAsc(Long idCaso);

    Optional<Documento> findByIdAndCaso_Id(Long id, Long idCaso);

    @Query("select coalesce(sum(d.tamanoBytes), 0) from Documento d where d.caso.id = :idCaso")
    long sumarTamanoPorCaso(@Param("idCaso") Long idCaso);
}
