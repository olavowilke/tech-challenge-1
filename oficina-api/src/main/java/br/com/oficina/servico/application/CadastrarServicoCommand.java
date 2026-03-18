package br.com.oficina.servico.application;

import java.math.BigDecimal;

public record CadastrarServicoCommand(
        String nome,
        String descricao,
        BigDecimal preco,
        int tempoEstimadoMinutos
) {}
