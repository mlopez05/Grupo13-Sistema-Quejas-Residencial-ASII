package com.portal.residencial.authservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.portal.residencial.authservice.bitacora.BitacoraClient;
import com.portal.residencial.authservice.casos.CasosClient;
import com.portal.residencial.authservice.dto.ActualizarPerfilRequest;
import com.portal.residencial.authservice.dto.CambiarClaveRequest;
import com.portal.residencial.authservice.dto.ActualizarUsuarioRequest;
import com.portal.residencial.authservice.dto.PersonalResponse;
import com.portal.residencial.authservice.dto.RegistroRequest;
import com.portal.residencial.authservice.dto.RegistroResponse;
import com.portal.residencial.authservice.dto.UsuarioResponse;
import com.portal.residencial.authservice.exception.BusinessException;
import com.portal.residencial.authservice.model.Rol;
import com.portal.residencial.authservice.model.Usuario;
import com.portal.residencial.authservice.notificacion.NotificacionClient;
import com.portal.residencial.authservice.repository.RolRepository;
import com.portal.residencial.authservice.repository.UsuarioRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsuarioService {

        private final UsuarioRepository usuarioRepository;
        private final RolRepository rolRepository;
        private final PasswordEncoder passwordEncoder;
        private final NotificacionClient notificacionClient;
        private final BitacoraClient bitacoraClient;
        private final JwtService jwtService;
        private final CasosClient casosClient;

        private static final String ROL_ANALISTA = "Analista";

        private static final int EDAD_MINIMA = 18;

        public RegistroResponse registroUsuarioResidente(RegistroRequest request) {

                if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
                        throw new BusinessException("El correo " + request.getCorreo() + " ya se encuentra registrado.",
                                        HttpStatus.CONFLICT);
                }

                if (usuarioRepository.findByDpi(request.getDpi()).isPresent()) {
                        throw new BusinessException("El DPI " + request.getDpi() + " ya se encuentra registrado.",
                                        HttpStatus.CONFLICT);
                }

                validarFechaNacimiento(request.getFecha_nacimiento());

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
                                .estado("Activo")
                                .build();

                Usuario usuarioRegistrado = usuarioRepository.save(usuario);

                RegistroResponse uResponse = RegistroResponse.builder()
                                .usuario(usuarioRegistrado.getCorreo())
                                .nombre(usuarioRegistrado.getNombre())
                                .apellido(usuarioRegistrado.getApellido())
                                .build();

                notificacionClient.enviarBienvenida(uResponse.getUsuario());

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

                validarFechaNacimiento(request.getFecha_nacimiento());

                Rol rol = rolRepository.findById(Long.valueOf(request.getId_rol()))
                                .orElseThrow(() -> new RuntimeException("Rol no encontrado."));

                /* LA CONTRASEÑA DEL PERSONAL ES TEMPORAL */
                Usuario usuario = Usuario.builder()
                                .debeCambiarClave(true)
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
                                .estado("Activo")
                                .build();

                Usuario usuarioRegistrado = usuarioRepository.save(usuario);

                /* ENVÍA SU USUARIO Y CONTRASEÑA TEMPORAL POR CORREO */
                notificacionClient.enviarBienvenidaPersonal(usuarioRegistrado.getCorreo(),
                                usuarioRegistrado.getNombre(), rol.getNombre(), request.getClave());

                RegistroResponse uResponse = RegistroResponse.builder()
                                .usuario(usuarioRegistrado.getCorreo())
                                .nombre(usuarioRegistrado.getNombre())
                                .apellido(usuarioRegistrado.getApellido())
                                .build();

                return uResponse;
        }

        /* SIEMPRE RESPONDE IGUAL PARA NO REVELAR QUÉ CORREOS ESTÁN REGISTRADOS */
        public void solicitarRecuperacion(String correo) {
                if (usuarioRepository.findByCorreo(correo).isEmpty()) {
                        log.info("Solicitud de recuperación para un correo no registrado.");
                        return;
                }

                String token = jwtService.generarTokenRecuperacion(correo);
                notificacionClient.enviarRecuperacionPassword(correo, token);
        }

        /* CAMBIO DE CONTRASEÑA DESDE EL PERFIL (PIDE LA ACTUAL) */
        public void cambiarClave(CambiarClaveRequest request, String ip) {
                Usuario usuario = obtenerUsuarioAutenticado();

                if (!passwordEncoder.matches(request.claveActual(), usuario.getClave())) {
                        throw new BusinessException("La contraseña actual no es correcta.", HttpStatus.BAD_REQUEST);
                }

                if (passwordEncoder.matches(request.claveNueva(), usuario.getClave())) {
                        throw new BusinessException("La nueva contraseña debe ser distinta a la actual.",
                                        HttpStatus.BAD_REQUEST);
                }

                usuario.setClave(passwordEncoder.encode(request.claveNueva()));
                usuario.setDebeCambiarClave(false); /* YA NO ES TEMPORAL */
                usuario.setFechaCambioClave(LocalDateTime.now());
                usuarioRepository.save(usuario);

                bitacoraClient.registrarCambioUsuario(
                                usuario.getId(), usuario.getNombre(), usuario.getApellido(),
                                "clave", "********", "********", "CAMBIO_CLAVE", ip,
                                usuario.getId(), usuario.getNombre(), usuario.getApellido());
        }

        /* RESTABLECE LA CONTRASEÑA CON EL ENLACE DEL CORREO */
        public void restablecerPassword(String token, String nuevaPassword, String ip) {
                Claims claims;
                try {
                        claims = jwtService.validarTokenRecuperacion(token);
                } catch (ExpiredJwtException e) {
                        throw new BusinessException("El enlace de recuperación ha expirado.", HttpStatus.BAD_REQUEST);
                } catch (JwtException e) {
                        throw new BusinessException("El enlace de recuperación no es válido.", HttpStatus.BAD_REQUEST);
                }

                Usuario usuario = usuarioRepository.findByCorreo(claims.getSubject())
                                .orElseThrow(() -> new BusinessException("El enlace de recuperación no es válido.",
                                                HttpStatus.BAD_REQUEST));

                /* EL ENLACE SOLO SIRVE UNA VEZ: SE INVALIDA SI LA CONTRASEÑA CAMBIÓ DESPUÉS DE EMITIRLO */
                if (usuario.getFechaCambioClave() != null && claims.getIssuedAt() != null) {
                        LocalDateTime emitido = LocalDateTime.ofInstant(claims.getIssuedAt().toInstant(), ZoneId.systemDefault());

                        if (emitido.isBefore(usuario.getFechaCambioClave().truncatedTo(ChronoUnit.SECONDS))) {
                                throw new BusinessException("Este enlace de recuperación ya fue utilizado. Solicita uno nuevo.",
                                                HttpStatus.BAD_REQUEST);
                        }
                }

                usuario.setClave(passwordEncoder.encode(nuevaPassword));
                usuario.setFechaCambioClave(LocalDateTime.now());
                /* AL RESTABLECERLA DEJA DE SER TEMPORAL */
                usuario.setDebeCambiarClave(false);
                usuarioRepository.save(usuario);

                /* LA CONTRASEÑA NUNCA VA A BITÁCORA: SOLO ********  */
                bitacoraClient.registrarCambioUsuario(
                                usuario.getId(), usuario.getNombre(), usuario.getApellido(),
                                "clave", "********", "********", "RESTABLECIMIENTO_CLAVE", ip,
                                usuario.getId(), usuario.getNombre(), usuario.getApellido());
        }

        /* REGISTRA EN BITÁCORA UN INICIO DE SESIÓN QUE NO SE PERMITIÓ */
        public void registrarIntentoFallido(String correo, String ip, String tipoEvento) {
                Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);

                bitacoraClient.registrarIntentoFallido(
                                usuario == null ? null : usuario.getId(),
                                correo,
                                usuario == null ? null : usuario.getNombre(),
                                usuario == null ? null : usuario.getApellido(),
                                usuario == null || usuario.getRol() == null ? null : usuario.getRol().getNombre(),
                                ip, tipoEvento);
        }

        /* LA FECHA DE NACIMIENTO NO PUEDE SER FUTURA Y DEBE SER MAYOR DE EDAD */
        private void validarFechaNacimiento(LocalDate fechaNacimiento) {
                if (fechaNacimiento == null) {
                        return;
                }

                if (fechaNacimiento.isAfter(LocalDate.now())) {
                        throw new BusinessException("La fecha de nacimiento no puede ser posterior a la fecha actual.",
                                        HttpStatus.BAD_REQUEST);
                }

                if (fechaNacimiento.plusYears(EDAD_MINIMA).isAfter(LocalDate.now())) {
                        throw new BusinessException("La persona debe tener al menos " + EDAD_MINIMA + " años.",
                                        HttpStatus.BAD_REQUEST);
                }
        }

        public List<UsuarioResponse> listarUsuarios() {
                return usuarioRepository.findAll(Sort.by("id")).stream()
                                .map(this::aUsuarioResponse)
                                .toList();
        }

        public UsuarioResponse obtenerUsuario(Long id) {
                Usuario usuario = usuarioRepository.findById(id)
                                .orElseThrow(() -> new BusinessException("Usuario no encontrado.",
                                                HttpStatus.NOT_FOUND));

                return aUsuarioResponse(usuario);
        }

        /* LISTA DE ANALISTAS O SUPERVISORES ACTIVOS */
        public List<PersonalResponse> listarPersonalActivo(String rol) {
                if (!"Analista".equals(rol) && !"Supervisor".equals(rol)) {
                        throw new BusinessException("Rol no permitido. Use 'Analista' o 'Supervisor'.",
                                        HttpStatus.BAD_REQUEST);
                }

                return usuarioRepository.findByRol_NombreOrderByIdAsc(rol).stream()
                                .filter(Usuario::isEnabled)
                                .map(usuario -> new PersonalResponse(
                                                usuario.getId(),
                                                usuario.getNombre(),
                                                usuario.getApellido(),
                                                usuario.getCorreo()))
                                .toList();
        }

        /* CONVIERTE EL USUARIO A RESPUESTA SIN LA CONTRASEÑA */
        private UsuarioResponse aUsuarioResponse(Usuario usuario) {
                String estado = usuario.getEstado() == null || usuario.getEstado().isBlank()
                                ? "Activo"
                                : usuario.getEstado();

                return new UsuarioResponse(
                                usuario.getId(),
                                usuario.getNombre(),
                                usuario.getApellido(),
                                usuario.getCorreo(),
                                usuario.getFecha_nacimiento(),
                                usuario.getDpi(),
                                usuario.getTelefono(),
                                usuario.getDireccion(),
                                usuario.getRol() != null ? usuario.getRol().getNombre() : null,
                                estado);
        }

        /* EL ADMINISTRADOR ACTUALIZA CUALQUIER USUARIO */
        public void actualizarUsuario(Long id, ActualizarUsuarioRequest request, String ip) {
                Usuario usuarioExistente = usuarioRepository.findById(id)
                                .orElseThrow(() -> new BusinessException("Usuario no encontrado.",
                                                HttpStatus.NOT_FOUND));

                Usuario usuarioModificador = obtenerUsuarioAutenticado();

                if ("Inactivo".equals(request.estado())
                                && usuarioExistente.getId().equals(usuarioModificador.getId())) {
                        throw new BusinessException("No puede inactivar su propia cuenta.",
                                        HttpStatus.BAD_REQUEST);
                }

                /* UN ANALISTA CON CASOS EN CURSO NO SE PUEDE INACTIVAR */
                if ("Inactivo".equals(request.estado()) && !"Inactivo".equals(usuarioExistente.getEstado())
                                && usuarioExistente.getRol() != null
                                && ROL_ANALISTA.equalsIgnoreCase(usuarioExistente.getRol().getNombre())) {
                        long casosEnCurso = casosClient.contarCasosEnCurso(usuarioExistente.getId());

                        if (casosEnCurso > 0) {
                                throw new BusinessException("El analista tiene " + casosEnCurso
                                                + (casosEnCurso == 1 ? " caso asignado en curso" : " casos asignados en curso")
                                                + ". Reasígnalos a otro analista antes de inactivarlo.",
                                                HttpStatus.CONFLICT);
                        }
                }

                aplicarActualizacion(usuarioExistente, usuarioModificador, request, ip);
        }

        /* DATOS DEL USUARIO QUE INICIÓ SESIÓN */
        public UsuarioResponse obtenerPerfil() {
                return aUsuarioResponse(obtenerUsuarioAutenticado());
        }

        /* EL USUARIO ACTUALIZA SU PROPIO PERFIL */
        public void actualizarPerfil(ActualizarPerfilRequest request, String ip) {
                Usuario usuario = obtenerUsuarioAutenticado();

                ActualizarUsuarioRequest cambios = new ActualizarUsuarioRequest(
                                request.nombreUsuario(),
                                request.apellidoUsuario(),
                                request.fechaNacimiento(),
                                null,
                                request.direccion(),
                                request.telefono(),
                                null);

                aplicarActualizacion(usuario, usuario, cambios, ip);
        }

        /* SOLO SE CAMBIAN Y REGISTRAN LOS CAMPOS QUE SE ENVÍAN */
        private void aplicarActualizacion(Usuario usuarioExistente, Usuario usuarioModificador,
                        ActualizarUsuarioRequest request, String ip) {

                if (request.nombreUsuario() == null && request.apellidoUsuario() == null
                                && request.fechaNacimiento() == null && request.dpi() == null
                                && request.direccion() == null && request.telefono() == null
                                && request.estado() == null) {
                        throw new BusinessException("Debe enviar al menos un campo a modificar.",
                                        HttpStatus.BAD_REQUEST);
                }

                if (request.dpi() != null && !request.dpi().equals(usuarioExistente.getDpi())
                                && usuarioRepository.findByDpi(request.dpi()).isPresent()) {
                        throw new BusinessException("El DPI " + request.dpi() + " ya se encuentra registrado.",
                                        HttpStatus.CONFLICT);
                }

                validarFechaNacimiento(request.fechaNacimiento());

                Long idModificado = usuarioExistente.getId();
                String nombreModificado = usuarioExistente.getNombre();
                String apellidoModificado = usuarioExistente.getApellido();
                Long idModificador = usuarioModificador.getId();
                String nombreModificador = usuarioModificador.getNombre();
                String apellidoModificador = usuarioModificador.getApellido();

                if (request.nombreUsuario() != null) {
                        registrarSiCambio(idModificado, nombreModificado, apellidoModificado,
                                        "nombre", usuarioExistente.getNombre(), request.nombreUsuario(), ip,
                                        idModificador, nombreModificador, apellidoModificador);
                        usuarioExistente.setNombre(request.nombreUsuario());
                }

                if (request.apellidoUsuario() != null) {
                        registrarSiCambio(idModificado, nombreModificado, apellidoModificado,
                                        "apellido", usuarioExistente.getApellido(), request.apellidoUsuario(), ip,
                                        idModificador, nombreModificador, apellidoModificador);
                        usuarioExistente.setApellido(request.apellidoUsuario());
                }

                if (request.fechaNacimiento() != null) {
                        registrarSiCambio(idModificado, nombreModificado, apellidoModificado,
                                        "fecha_nacimiento", Objects.toString(usuarioExistente.getFecha_nacimiento(), null),
                                        request.fechaNacimiento().toString(), ip,
                                        idModificador, nombreModificador, apellidoModificador);
                        usuarioExistente.setFecha_nacimiento(request.fechaNacimiento());
                }

                if (request.dpi() != null) {
                        registrarSiCambio(idModificado, nombreModificado, apellidoModificado,
                                        "dpi", usuarioExistente.getDpi(), request.dpi(), ip,
                                        idModificador, nombreModificador, apellidoModificador);
                        usuarioExistente.setDpi(request.dpi());
                }

                if (request.direccion() != null) {
                        registrarSiCambio(idModificado, nombreModificado, apellidoModificado,
                                        "direccion", usuarioExistente.getDireccion(), request.direccion(), ip,
                                        idModificador, nombreModificador, apellidoModificador);
                        usuarioExistente.setDireccion(request.direccion());
                }

                if (request.telefono() != null) {
                        registrarSiCambio(idModificado, nombreModificado, apellidoModificado,
                                        "telefono", usuarioExistente.getTelefono(), request.telefono(), ip,
                                        idModificador, nombreModificador, apellidoModificador);
                        usuarioExistente.setTelefono(request.telefono());
                }

                if (request.estado() != null) {
                        registrarSiCambio(idModificado, nombreModificado, apellidoModificado,
                                        "estado", usuarioExistente.getEstado(), request.estado(), ip,
                                        idModificador, nombreModificador, apellidoModificador);
                        usuarioExistente.setEstado(request.estado());
                }

                usuarioRepository.save(usuarioExistente);
        }

        private void registrarSiCambio(Long idUsuarioModificado, String nombreUsuarioModificado,
                        String apellidoUsuarioModificado,
                        String campoModificado, String valorAnterior, String valorNuevo,
                        String ipUsuarioModificador,
                        Long idUsuarioModificador, String nombreUsuarioModificador, String apellidoUsuarioModificador) {
                boolean cambio = valorAnterior == null
                                ? valorNuevo != null
                                : !valorAnterior.equals(valorNuevo);

                if (cambio) {
                        bitacoraClient.registrarCambioUsuario(
                                        idUsuarioModificado,
                                        nombreUsuarioModificado,
                                        apellidoUsuarioModificado,
                                        campoModificado,
                                        valorAnterior,
                                        valorNuevo,
                                        "ACTUALIZACIÓN",
                                        ipUsuarioModificador,
                                        idUsuarioModificador,
                                        nombreUsuarioModificador,
                                        apellidoUsuarioModificador);
                }
        }

        /* BUSCA AL USUARIO DE LA SESIÓN EN LA BASE */
        private Usuario obtenerUsuarioAutenticado() {
                Usuario principal = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

                return usuarioRepository.findByCorreo(principal.getCorreo())
                                .orElseThrow(() -> new BusinessException("Usuario no encontrado.",
                                                HttpStatus.NOT_FOUND));
        }

        /* CREA EL USUARIO ADMINISTRADOR */
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
