package com.hibob.anyim.agw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.hibob.anyim.agw", "com.hibob.anyim.common"})
@SpringBootApplication(exclude = {ReactiveUserDetailsServiceAutoConfiguration.class}) // 禁用secrity，不需要Security的登录，会自己实现，因为web-application-type: reactive，所以要排除这个配置
public class AgwApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgwApplication.class, args);

    }
}
