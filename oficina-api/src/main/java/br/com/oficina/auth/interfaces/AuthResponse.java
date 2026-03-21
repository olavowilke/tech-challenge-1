package br.com.oficina.auth.interfaces;

public record AuthResponse(String token, String username, String role, long expiresInMs) {}
