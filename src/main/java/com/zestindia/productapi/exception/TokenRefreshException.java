package com.zestindia.productapi.exception;

public class TokenRefreshException extends RuntimeException {
	public TokenRefreshException(String token, String message) {
		super(String.format("Failed for refresh token [%s]: %s", token, message));
	}
}
