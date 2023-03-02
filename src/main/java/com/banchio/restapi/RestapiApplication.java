package com.banchio.restapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.microsoft.applicationinsights.attach.ApplicationInsights;



@SpringBootApplication
public class RestapiApplication {

	public static void main(String[] args) {
		ApplicationInsights.attach();
		SpringApplication.run(RestapiApplication.class, args);
	}

}
