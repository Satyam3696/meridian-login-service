package com.insurance.loginapp.config;

import com.insurance.loginapp.model.User;
import com.insurance.loginapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Seeds one demo user on startup so you can log in immediately without
// registering first. Safe to delete once you connect to a real RDS DB.
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("demo")) {
            userRepository.save(new User("demo", passwordEncoder.encode("demo1234"), "ROLE_USER"));
            System.out.println(">>> Seeded demo user -> username: demo / password: demo1234");
        }
    }
}
