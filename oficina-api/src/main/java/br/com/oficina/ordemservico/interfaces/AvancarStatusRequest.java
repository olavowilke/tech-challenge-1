package br.com.oficina.ordemservico.interfaces;

import br.com.oficina.ordemservico.domain.StatusOS;
import jakarta.validation.constraints.NotNull;

public record AvancarStatusRequest(@NotNull StatusOS novoStatus) {}
