package com.task.needmoretask;

import com.task.needmoretask.core.util.Timestamped;
import com.task.needmoretask.model.profile.Profile;
import com.task.needmoretask.model.profile.ProfileRepository;
import com.task.needmoretask.model.user.User;
import com.task.needmoretask.model.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@EnableJpaAuditing(dateTimeProviderRef = "zonedDateTimeProvider")
@SpringBootApplication
public class NeedMoreTaskApplication {
    @Bean
    CommandLineRunner initData(BCryptPasswordEncoder passwordEncoder, UserRepository userRepository, ProfileRepository profileRepository){
        return args -> {
            Profile profile = profileRepository.save(new Profile(null, "img.jpg"));
            User user = User.builder()
                    .email("tester@email.com")
                    .password(passwordEncoder.encode("123456"))
                    .phone("010-0000-0000")
                    .fullname("tester")
                    .department(User.Department.HR)
                    .joinCompanyYear(2023)
                    .profile(profile)
                    .role(User.Role.USER)
                    .build();
            User admin = User.builder()
                    .email("admin@email.com")
                    .password(passwordEncoder.encode("123456"))
                    .phone("010-0000-0000")
                    .fullname("admin")
                    .department(User.Department.DEVELOPMENT)
                    .joinCompanyYear(2024)
                    .profile(profile)
                    .role(User.Role.ADMIN)
                    .build();
            List<User> users = List.of(user,admin);
            userRepository.saveAll(users);
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(NeedMoreTaskApplication.class, args);
    }

    @Bean
    public DateTimeProvider zonedDateTimeProvider(){
        return () -> Optional.of(ZonedDateTime.now(Timestamped.SEOUL_ZONE_ID));
    }

}
