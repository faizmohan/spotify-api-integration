package com.faiz.spotify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SpotifyApiIntegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpotifyApiIntegrationApplication.class, args);
	}
	
	@Bean
	public RestTemplate restTemplate() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(factory);
		return restTemplate;
	}

}
