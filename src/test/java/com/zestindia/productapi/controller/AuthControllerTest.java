package com.zestindia.productapi.controller;

import com.zestindia.productapi.dto.AuthRequest;
import com.zestindia.productapi.dto.AuthResponse;
import com.zestindia.productapi.dto.RefreshTokenRequest;
import com.zestindia.productapi.entity.RefreshToken;
import com.zestindia.productapi.entity.User;
import com.zestindia.productapi.exception.TokenRefreshException;
import com.zestindia.productapi.repository.UserRepository;
import com.zestindia.productapi.security.JwtUtil;
import com.zestindia.productapi.service.RefreshTokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private UserDetailsService userDetailsService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private RefreshTokenService refreshTokenService;

	@InjectMocks
	private AuthController authController;

	private User user;

	private UserDetails userDetails;

	private RefreshToken refreshToken;

	@BeforeEach
	void setUp() {

		user = User.builder().username("admin").password("password").role(User.Role.ROLE_ADMIN).build();

		userDetails = org.springframework.security.core.userdetails.User.withUsername("admin").password("password")
				.roles("ADMIN").build();

		refreshToken = new RefreshToken();

		refreshToken.setToken("old-refresh-token");
		refreshToken.setUser(user);
	}

	// ---------------------------------------------------------
	// LOGIN SUCCESS
	// ---------------------------------------------------------

	@Test
	void login_shouldReturnAccessAndRefreshTokens() {

		AuthRequest request = new AuthRequest("admin", "password");

		when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);

		when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

		when(jwtUtil.generateAccessToken(userDetails)).thenReturn("access-token");

		when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

		ResponseEntity<AuthResponse> response = authController.login(request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		assertNotNull(response.getBody());

		assertEquals("access-token", response.getBody().getAccessToken());

		assertEquals("old-refresh-token", response.getBody().getRefreshToken());

		verify(authenticationManager).authenticate(any());

		verify(userDetailsService).loadUserByUsername("admin");

		verify(userRepository).findByUsername("admin");

		verify(jwtUtil).generateAccessToken(userDetails);

		verify(refreshTokenService).createRefreshToken(user);
	}

	// ---------------------------------------------------------
	// LOGIN USER NOT FOUND
	// ---------------------------------------------------------

	@Test
	void login_shouldThrowException_whenUserNotFound() {

		AuthRequest request = new AuthRequest("admin", "password");

		when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);

		when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

		IllegalStateException exception = assertThrows(IllegalStateException.class,
				() -> authController.login(request));

		assertEquals("User vanished after authentication", exception.getMessage());

		verify(userRepository).findByUsername("admin");

		verify(jwtUtil, never()).generateAccessToken(any());

		verify(refreshTokenService, never()).createRefreshToken(any());
	}

	// ---------------------------------------------------------
	// REFRESH TOKEN SUCCESS
	// ---------------------------------------------------------

	@Test
	void refresh_shouldReturnNewTokens() {

		RefreshTokenRequest request = new RefreshTokenRequest();

		request.setRefreshToken("old-refresh-token");

		RefreshToken newRefreshToken = new RefreshToken();

		newRefreshToken.setToken("new-refresh-token");

		newRefreshToken.setUser(user);

		when(refreshTokenService.findByToken("old-refresh-token")).thenReturn(Optional.of(refreshToken));

		when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);

		when(refreshTokenService.createRefreshToken(user)).thenReturn(newRefreshToken);

		when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);

		when(jwtUtil.generateAccessToken(userDetails)).thenReturn("new-access-token");

		ResponseEntity<AuthResponse> response = authController.refresh(request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		assertNotNull(response.getBody());

		assertEquals("new-access-token", response.getBody().getAccessToken());

		assertEquals("new-refresh-token", response.getBody().getRefreshToken());

		verify(refreshTokenService).findByToken("old-refresh-token");

		verify(refreshTokenService).verifyExpiration(refreshToken);

		verify(refreshTokenService).createRefreshToken(user);

		verify(jwtUtil).generateAccessToken(userDetails);
	}

	// ---------------------------------------------------------
	// REFRESH TOKEN NOT FOUND
	// ---------------------------------------------------------

	@Test
	void refresh_shouldThrowException_whenTokenNotFound() {

		RefreshTokenRequest request = new RefreshTokenRequest();

		request.setRefreshToken("invalid-token");

		when(refreshTokenService.findByToken("invalid-token")).thenReturn(Optional.empty());

		assertThrows(TokenRefreshException.class, () -> authController.refresh(request));

		verify(refreshTokenService).findByToken("invalid-token");

		verify(refreshTokenService, never()).verifyExpiration(any());

		verify(refreshTokenService, never()).createRefreshToken(any());

		verify(jwtUtil, never()).generateAccessToken(any());
	}
}