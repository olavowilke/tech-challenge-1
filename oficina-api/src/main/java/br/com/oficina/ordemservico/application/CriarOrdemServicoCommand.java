package br.com.oficina.ordemservico.application;

import java.util.UUID;

public record CriarOrdemServicoCommand(UUID clienteId, UUID veiculoId, String observacoes) {}
