package org.cinos.authin_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AuthinCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthinCoreApplication.class, args);
	}

}
