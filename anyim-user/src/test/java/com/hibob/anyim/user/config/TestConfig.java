package com.hibob.anyim.user.config;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TestConfig {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public TestRestTemplate testRestTemplate(RestTemplateBuilder restTemplateBuilder){
        return new TestRestTemplate(restTemplateBuilder);
    }
}
