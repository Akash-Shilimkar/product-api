package com.zestindia.productapi.service;

import com.zestindia.productapi.entity.RefreshToken;
import com.zestindia.productapi.entity.User;
import com.zestindia.productapi.exception.TokenRefreshException;
import com.zestindia.productapi.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;

	@Value("${app.jwt.refresh-token-expiration-ms}")
	private long refreshTokenExpirationMs;

	@Transactional
	public RefreshToken createRefreshToken(User user) {
		// Rotation: invalidate any previous refresh token for this user first.
		refreshTokenRepository.deleteByUser(user);

		RefreshToken refreshToken = RefreshToken.builder().user(user).token(UUID.randomUUID().toString())
				.expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs)).build();

		return refreshTokenRepository.save(refreshToken);
	}

	public Optional<RefreshToken> findByToken(String token) {
		return refreshTokenRepository.findByToken(token);
	}

	public RefreshToken verifyExpiration(RefreshToken token) {
		if (token.getExpiryDate().isBefore(Instant.now())) {
			refreshTokenRepository.delete(token);
			throw new TokenRefreshException(token.getToken(), "Refresh token expired. Please sign in again.");
		}
		return token;
	}
}
