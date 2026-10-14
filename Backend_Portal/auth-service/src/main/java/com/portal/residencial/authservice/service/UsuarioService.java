package com.portal.residencial.authservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.portal.residencial.authservice.dto.CorreoRequest;
import com.portal.residencial.authservice.dto.RegistroRequest;
import com.portal.residencial.authservice.dto.RegistroResponse;
import com.portal.residencial.authservice.exception.BusinessException;
import com.portal.residencial.authservice.model.Rol;
import com.portal.residencial.authservice.model.Usuario;
import com.portal.residencial.authservice.notificacion.NotificacionClient;
import com.portal.residencial.authservice.repository.RolRepository;
import com.portal.residencial.authservice.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

        private final UsuarioRepository usuarioRepository;
        private final RolRepository rolRepository;
        private final PasswordEncoder passwordEncoder;
        private final NotificacionClient notificacionClient;

        public RegistroResponse registroUsuarioResidente(RegistroRequest request) {

                if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
                        throw new BusinessException("El correo " + request.getCorreo() + " ya se encuentra registrado.",
                                        HttpStatus.CONFLICT);
                }

                if (usuarioRepository.findByDpi(request.getDpi()).isPresent()) {
                        throw new BusinessException("El DPI " + request.getDpi() + " ya se encuentra registrado.",
                                        HttpStatus.CONFLICT);
                }

                Rol rol = rolRepository.findById(Long.valueOf(4))
                                .orElseThrow(() -> new RuntimeException("Rol no encontrado."));

                Usuario usuario = Usuario.builder()
                                .nombre(request.getNombre())
                                .apellido(request.getApellido())
                                .correo(request.getCorreo())
                                .clave(passwordEncoder.encode(request.getClave()))
                                .fecha_nacimiento(request.getFecha_nacimiento())
                                .dpi(request.getDpi())
                                .telefono(request.getTelefono())
                                .rol(rol)
                                .direccion(request.getDireccion())
                                .fecha_registro(LocalDate.now())
                                .hora_registro(LocalTime.now())
                                .build();

                Usuario usuarioRegistrado = usuarioRepository.save(usuario);

                RegistroResponse uResponse = RegistroResponse.builder()
                                .usuario(usuarioRegistrado.getCorreo())
                                .nombre(usuarioRegistrado.getNombre())
                                .apellido(usuarioRegistrado.getApellido())
                                .build();

                return uResponse;
        }

        public RegistroResponse registroUsuarioPortal(RegistroRequest request) {

                if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
                        throw new BusinessException("El correo " + request.getCorreo() + " ya se encuentra registrado.",
                                        HttpStatus.CONFLICT);
                }

                if (usuarioRepository.findByDpi(request.getDpi()).isPresent()) {
                        throw new BusinessException("El DPI " + request.getDpi() + " ya se encuentra registrado.",
                                        HttpStatus.CONFLICT);
                }

                Rol rol = rolRepository.findById(Long.valueOf(request.getId_rol()))
                                .orElseThrow(() -> new RuntimeException("Rol no encontrado."));

                Usuario usuario = Usuario.builder()
                                .nombre(request.getNombre())
                                .apellido(request.getApellido())
                                .correo(request.getCorreo())
                                .clave(passwordEncoder.encode(request.getClave()))
                                .fecha_nacimiento(request.getFecha_nacimiento())
                                .dpi(request.getDpi())
                                .rol(rol)
                                .direccion(request.getDireccion())
                                .fecha_registro(LocalDate.now())
                                .hora_registro(LocalTime.now())
                                .build();

                Usuario usuarioRegistrado = usuarioRepository.save(usuario);

                RegistroResponse uResponse = RegistroResponse.builder()
                                .usuario(usuarioRegistrado.getCorreo())
                                .nombre(usuarioRegistrado.getNombre())
                                .apellido(usuarioRegistrado.getApellido())
                                .build();

                return uResponse;
        }

        public void solicitarRecuperacion(String correo) {
                Usuario usuario = usuarioRepository.findByCorreo(correo)
                                .orElseThrow(() -> new BusinessException(
                                                "El correo ingresado no está registrado en el sistema.",
                                                HttpStatus.NOT_FOUND));

                String token = UUID.randomUUID().toString();

                PasswordResetToken resetToken = new PasswordResetToken();
                resetToken.setToken(token);
                resetToken.setCorreoUsuario(correo);
                resetToken.setExpiracion(LocalDateTime.now().plusMinutes(30));
                tokenRepository.save(resetToken);

                notificacionClient.enviarRecuperacionPassword(correo, token);
        }

        /* Método para crear el usuario admin */
        /*
         * public void registroAdmin(){
         * 
         * Rol rol = rolRepository.findById(Long.valueOf(1)).orElseThrow(() -> new
         * RuntimeException("Rol no encontrado."));
         * 
         * Usuario admin = Usuario.builder()
         * .nombre("Marcos")
         * .apellido("Lopez")
         * .correo("mlopez@portal.com")
         * .clave(passwordEncoder.encode("8888"))
         * .fecha_nacimiento(LocalDate.now())
         * .dpi("33344668899")
         * .rol(rol)
         * .direccion("Ciudad de Guatemala")
         * .fecha_registro(LocalDate.now())
         * .hora_registro(LocalTime.now())
         * .build();
         * 
         * usuarioRepository.save(admin);
         * }
         */
}
