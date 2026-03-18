package br.com.oficina.peca.application;

import java.math.BigDecimal;

public record AtualizarPecaCommand(
        String nome,
        String descricao,
        BigDecimal precoUnitario
) {}
