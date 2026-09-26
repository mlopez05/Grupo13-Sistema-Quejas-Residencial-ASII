package com.portal.residencial.authservice.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.portal.residencial.authservice.model.Rol;
import com.portal.residencial.authservice.repository.RolRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RolService {
    private final RolRepository rolRepository;

    public Optional<Rol> buscaRol(Long id){
        return rolRepository.findById(id);
    }
}
