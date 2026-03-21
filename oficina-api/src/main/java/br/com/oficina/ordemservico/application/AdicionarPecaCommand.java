package br.com.oficina.ordemservico.application;

import java.util.UUID;

public record AdicionarPecaCommand(UUID ordemServicoId, UUID pecaId, int quantidade) {}
