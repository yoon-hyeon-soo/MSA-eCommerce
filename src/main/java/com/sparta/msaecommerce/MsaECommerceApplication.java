package com.sparta.msaecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MsaECommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsaECommerceApplication.class, args);
    }

}
