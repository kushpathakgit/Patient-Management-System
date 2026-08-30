package com.pm.authservice;

import com.pm.authservice.model.User;
import com.pm.authservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner seedDefaultUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> userRepository.findByEmail("testuser@test.com")
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail("testuser@test.com");
                    user.setPassword(passwordEncoder.encode("password123"));
                    user.setRole("ADMIN");
                    return userRepository.save(user);
                });
    }
}
