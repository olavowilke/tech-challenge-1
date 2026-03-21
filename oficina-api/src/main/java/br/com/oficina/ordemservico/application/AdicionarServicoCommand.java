package br.com.oficina.ordemservico.application;

import java.util.UUID;

public record AdicionarServicoCommand(UUID ordemServicoId, UUID servicoId) {}
