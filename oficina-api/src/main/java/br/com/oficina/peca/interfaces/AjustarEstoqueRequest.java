package br.com.oficina.peca.interfaces;

import jakarta.validation.constraints.NotNull;

public record AjustarEstoqueRequest(
        @NotNull Integer quantidade
) {}
