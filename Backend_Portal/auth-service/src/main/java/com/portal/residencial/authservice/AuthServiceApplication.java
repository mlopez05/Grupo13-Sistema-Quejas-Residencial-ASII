package com.portal.residencial.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

	/* 
	@Bean
    public CommandLineRunner initData(UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        return args -> {
                usuarioService.registroAdmin();
        };
    }
	*/

}
