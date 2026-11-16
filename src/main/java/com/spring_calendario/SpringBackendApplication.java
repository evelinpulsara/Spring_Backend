package com.spring_calendario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@EnableScheduling // latido de las conexiones SSE (tiemporeal.InMemoryEventBus)
@ConfigurationPropertiesScan
public class SpringBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBackendApplication.class, args);
	}

}
