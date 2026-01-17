package com.alexistdev.geobill;

import com.alexistdev.geobill.utils.AuditorAwareImpl;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@SpringBootApplication
@EnableJpaRepositories
@EnableJpaAuditing
@EntityScan
public class GeobillApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeobillApplication.class, args);
	}

	@Bean
	public AuditorAware<String> auditorAware() {
		return new AuditorAwareImpl();
	}

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		modelMapper.createTypeMap(Date.class, LocalDateTime.class)
				.setConverter(
						context -> context.getSource() == null ? null : LocalDateTime.ofInstant(
						context.getSource().toInstant(),
						ZoneId.systemDefault()));

		return modelMapper;
	}

}
