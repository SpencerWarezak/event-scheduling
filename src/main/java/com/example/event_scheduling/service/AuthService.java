package com.example.event_scheduling.service;

import com.example.event_scheduling.model.User;
import com.example.event_scheduling.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    public AuthService(UserRepository repository,
                       PasswordEncoder encoder) {
        this.userRepository = repository;
        this.encoder = encoder;
    }

    public User authenticate(String email, String password) {
        logger.info("Authenticating account for {}", email);
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !encoder.matches(password, user.getPassword())) {
            logger.info("User unable to be properly authenticated for {}", email);
            return null;
        }

        logger.info("User {} successfully authenticated", email);
        return user;
    }

    public User signup(String email,
                       String firstName,
                       String lastName,
                       String password) {
        logger.info("Creating new user {}", email);
        if (userRepository.findByEmail(email).isPresent()) {
            return null;
        }

        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(encoder.encode(password));

        userRepository.save(user);
        return user;
    }
}
