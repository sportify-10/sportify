package com.sparta.sportify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SportifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportifyApplication.class, args);
    }

}
