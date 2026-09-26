package com.examen.userservice;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    ModelMapper mapper(){
        return  new ModelMapper();
    }


    @Value("${config.mykey}")
    String myValue;

    @Override
    public void run(String... args) throws Exception {
     System.out.println("Got Properties from config server::"+myValue);
    }
}
