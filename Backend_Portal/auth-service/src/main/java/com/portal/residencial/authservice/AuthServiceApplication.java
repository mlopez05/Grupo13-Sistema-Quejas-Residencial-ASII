package com.portal.residencial.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableAsync /* PERMITE QUE LOS ENVÍOS A BITÁCORA Y NOTIFICACIONES SE HAGAN EN SEGUNDO PLANO */
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

	@Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
