package com.portal.residencial.contenido_service.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }
}
