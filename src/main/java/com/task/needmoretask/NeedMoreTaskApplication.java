package com.task.needmoretask;

import com.task.needmoretask.core.util.Timestamped;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.ZonedDateTime;
import java.util.Optional;

@EnableJpaAuditing(dateTimeProviderRef = "zonedDateTimeProvider")
@SpringBootApplication
public class NeedMoreTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeedMoreTaskApplication.class, args);
    }

    @Bean
    public DateTimeProvider zonedDateTimeProvider(){
        return () -> Optional.of(ZonedDateTime.now(Timestamped.SEOUL_ZONE_ID));
    }

}
