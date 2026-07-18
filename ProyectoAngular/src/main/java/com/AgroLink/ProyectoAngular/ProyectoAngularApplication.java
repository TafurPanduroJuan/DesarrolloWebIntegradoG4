package com.AgroLink.ProyectoAngular;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProyectoAngularApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProyectoAngularApplication.class, args);
	}

}
