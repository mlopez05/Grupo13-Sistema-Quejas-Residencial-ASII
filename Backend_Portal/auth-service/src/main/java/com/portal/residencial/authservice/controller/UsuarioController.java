package com.portal.residencial.authservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.portal.residencial.authservice.dto.ActualizarPerfilRequest;
import com.portal.residencial.authservice.dto.ActualizarUsuarioRequest;
import com.portal.residencial.authservice.dto.PersonalResponse;
import com.portal.residencial.authservice.dto.RegistroRequest;
import com.portal.residencial.authservice.dto.RegistroResponse;
import com.portal.residencial.authservice.dto.UsuarioResponse;
import com.portal.residencial.authservice.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/registro")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/residente")
    public ResponseEntity<RegistroResponse> guardar(@Valid @RequestBody RegistroRequest request){
        return ResponseEntity.ok(usuarioService.registroUsuarioResidente(request));
    }

    @PostMapping("/staff")
    public ResponseEntity<RegistroResponse> guardarAdmin(@Valid @RequestBody RegistroRequest request){
        return ResponseEntity.ok(usuarioService.registroUsuarioPortal(request));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios(){
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioResponse> obtenerUsuario(@PathVariable Long id){
        return ResponseEntity.ok(usuarioService.obtenerUsuario(id));
    }

    /* LISTA DEL PERSONAL ACTIVO PARA ASIGNAR CASOS */
    @GetMapping("/personal")
    public ResponseEntity<List<PersonalResponse>> listarPersonal(@RequestParam String rol){
        return ResponseEntity.ok(usuarioService.listarPersonalActivo(rol));
    }

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> obtenerPerfil(){
        return ResponseEntity.ok(usuarioService.obtenerPerfil());
    }

    @PutMapping("/perfil")
    public ResponseEntity<Void> actualizarPerfil(@Valid @RequestBody ActualizarPerfilRequest request,
                                                    HttpServletRequest httpRequest){
        usuarioService.actualizarPerfil(request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok().build();
    }

    @PutMapping ("/actualizar/{id}")
    public ResponseEntity<Void> actualizarUsuario(@PathVariable Long id,
                                                    @Valid @RequestBody ActualizarUsuarioRequest request, HttpServletRequest httpRequest){
        usuarioService.actualizarUsuario(id, request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok().build();
    }

}
