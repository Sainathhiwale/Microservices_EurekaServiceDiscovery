package com.examen.productcatlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ProductCatlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductCatlogApplication.class, args);
    }

}
