package br.com.oficina.auth.application;

import br.com.oficina.auth.domain.Role;

public record RegisterCommand(String username, String password, Role role) {}
