package com.portal.residencial.casos_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portal.residencial.casos_service.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByActivoTrueOrderByNombreAsc();

    List<Categoria> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    boolean existsByNombreIgnoreCase(String nombre);
}
