package com.zestindia.productapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

	@NotBlank(message = "username must not be blank")
	@Size(min = 3, max = 100, message = "username must be between 3 and 100 characters")
	private String username;

	@NotBlank(message = "password must not be blank")
	@Size(min = 6, message = "password must be at least 6 characters")
	private String password;

	/**
	 * Optional. Defaults to ROLE_USER if omitted or blank. Accepts "ADMIN" or
	 * "USER" (case-insensitive) - not the ROLE_ prefix.
	 */
	private String role;
}