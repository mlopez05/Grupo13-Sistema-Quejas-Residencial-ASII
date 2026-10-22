package com.portal.residencial.authservice.config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.portal.residencial.authservice.model.Rol;
import com.portal.residencial.authservice.model.Usuario;
import com.portal.residencial.authservice.repository.RolRepository;
import com.portal.residencial.authservice.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/* CARGA LOS ROLES Y EL PRIMER ADMINISTRADOR EN UNA INSTALACIÓN NUEVA (SOLO SI NO EXISTEN) */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatosIniciales implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.inicial.correo:}")
    private String correoAdmin;

    @Value("${admin.inicial.clave:}")
    private String claveAdmin;

    /* EL ORDEN DEFINE EL ID DE CADA ROL: 1 ADMINISTRADOR, 2 ANALISTA, 3 SUPERVISOR, 4 RESIDENTE */
    private static final List<String[]> ROLES = List.of(
            new String[] { "Administrador", "Gestiona usuarios, contenido del portal y catálogo" },
            new String[] { "Analista", "Analiza, gestiona y cierra los casos asignados" },
            new String[] { "Supervisor", "Autoriza o rechaza casos y los reasigna" },
            new String[] { "Residente", "Registra y da seguimiento a sus casos" });

    @Override
    @Transactional
    public void run(String... args) {
        if (rolRepository.count() == 0) {
            ROLES.forEach(r -> rolRepository.save(Rol.builder().nombre(r[0]).descripcion(r[1]).build()));
            log.info("Roles iniciales creados.");
        }

        if (!usuarioRepository.findByRol_NombreOrderByIdAsc("Administrador").isEmpty()) {
            return;
        }

        if (correoAdmin.isBlank() || claveAdmin.isBlank()) {
            log.warn("No hay ningún Administrador. Defina ADMIN_CORREO y ADMIN_CLAVE para crear el primero.");
            return;
        }

        Rol administrador = rolRepository.findAll().stream()
                .filter(r -> "Administrador".equals(r.getNombre()))
                .findFirst()
                .orElseThrow();

        /* LA CONTRASEÑA ES TEMPORAL: SE CAMBIA EN EL PRIMER INICIO DE SESIÓN */
        usuarioRepository.save(Usuario.builder()
                .nombre("Administrador")
                .apellido("Principal")
                .correo(correoAdmin.trim())
                .clave(passwordEncoder.encode(claveAdmin))
                .rol(administrador)
                .estado("Activo")
                .debeCambiarClave(true)
                .fecha_registro(LocalDate.now())
                .hora_registro(LocalTime.now())
                .build());

        log.info("Administrador inicial creado: {}", correoAdmin.trim());
    }
}
