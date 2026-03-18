package br.com.oficina.cliente.interfaces;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AtualizarClienteRequest(
        @NotBlank String nome,
        @Email String email,
        String telefone
) {}
