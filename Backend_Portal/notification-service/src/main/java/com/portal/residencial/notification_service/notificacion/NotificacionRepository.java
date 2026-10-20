package com.portal.residencial.notification_service.notificacion;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findTop30ByCorreoDestinatarioOrderByFechaDescIdDesc(String correoDestinatario);

    List<Notificacion> findByCorreoDestinatarioAndLeidaFalseOrderByFechaDescIdDesc(String correoDestinatario);

    /* MARCA COMO LEÍDAS LAS ALERTAS DEL USUARIO */

    @Modifying
    @Transactional
    @Query("update Notificacion n set n.leida = true, n.fechaLectura = :ahora " +
           "where n.id = :id and n.correoDestinatario = :correo and n.leida = false")
    int marcarLeida(@Param("id") Long id, @Param("correo") String correo, @Param("ahora") LocalDateTime ahora);

    @Modifying
    @Transactional
    @Query("update Notificacion n set n.leida = true, n.fechaLectura = :ahora " +
           "where n.idCaso = :idCaso and n.correoDestinatario = :correo and n.leida = false")
    int marcarLeidasPorCaso(@Param("idCaso") Long idCaso, @Param("correo") String correo,
            @Param("ahora") LocalDateTime ahora);

    @Modifying
    @Transactional
    @Query("update Notificacion n set n.leida = true, n.fechaLectura = :ahora " +
           "where n.correoDestinatario = :correo and n.leida = false")
    int marcarTodasLeidas(@Param("correo") String correo, @Param("ahora") LocalDateTime ahora);
}
