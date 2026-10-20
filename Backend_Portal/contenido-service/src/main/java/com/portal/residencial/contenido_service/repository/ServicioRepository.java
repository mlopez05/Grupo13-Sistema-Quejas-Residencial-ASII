package com.portal.residencial.contenido_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portal.residencial.contenido_service.model.Servicio;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findByActivoTrueOrderByOrdenAscNombreAsc();

    List<Servicio> findAllByOrderByOrdenAscNombreAsc();
}
