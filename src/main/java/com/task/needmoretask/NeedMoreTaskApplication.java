package com.task.needmoretask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class NeedMoreTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeedMoreTaskApplication.class, args);
    }

}
