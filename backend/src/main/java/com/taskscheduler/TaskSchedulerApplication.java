package com.taskscheduler;

import com.taskscheduler.entity.User;
import com.taskscheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class TaskSchedulerApplication {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(TaskSchedulerApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadDemoData() {
        return args -> {
            try {
                if (userRepository.findByEmail("demo@example.com").isEmpty()) {
                    User demoUser = new User();
                    demoUser.setEmail("demo@example.com");
                    demoUser.setName("Demo User");
                    demoUser.setPassword(passwordEncoder.encode("demo123"));
                    demoUser.setCreatedAt(LocalDateTime.now());
                    userRepository.save(demoUser);
                    log.info("Demo user created with ID: {}", demoUser.getId());
                }
            } catch (Exception e) {
                log.warn("Could not create demo user: {}", e.getMessage());
            }
        };
    }
}
