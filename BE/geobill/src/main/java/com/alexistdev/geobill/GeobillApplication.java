package com.alexistdev.geobill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@EntityScan
public class GeobillApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeobillApplication.class, args);
	}

//	@Bean
//	public AuditorAware<String> auditorAware(){
//		return new AuditorAwareImpl();
//	}
}
