package com.zestindia.productapi.config;

import com.zestindia.productapi.entity.User;
import com.zestindia.productapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default ADMIN and USER account on startup purely so the API is
 * immediately testable via Swagger/Postman. The assignment spec does not define
 * a registration endpoint, so accounts are provisioned here instead. Replace
 * with a proper registration flow / IdP integration in production.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) {
		if (userRepository.findByUsername("admin").isEmpty()) {
			userRepository.save(User.builder().username("admin").password(passwordEncoder.encode("Admin@123"))
					.role(User.Role.ROLE_ADMIN).build());
		}

		if (userRepository.findByUsername("user").isEmpty()) {
			userRepository.save(User.builder().username("user").password(passwordEncoder.encode("User@123"))
					.role(User.Role.ROLE_USER).build());
		}
	}
}
