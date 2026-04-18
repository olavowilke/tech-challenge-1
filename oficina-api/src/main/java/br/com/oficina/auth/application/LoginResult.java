package br.com.oficina.auth.application;

import br.com.oficina.auth.domain.Role;

public record LoginResult(String token, String username, Role role) {}
