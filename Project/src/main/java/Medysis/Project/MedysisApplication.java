package Medysis.Project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MedysisApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedysisApplication.class, args);
	}

}
