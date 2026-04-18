package br.com.oficina.ordemservico.domain;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

public interface OrdemServicoRepository {

    OptionalDouble tempoMedioExecucaoMinutos();


    OrdemServico save(OrdemServico ordemServico);

    Optional<OrdemServico> findById(UUID id);

    List<OrdemServico> findAll();

    List<OrdemServico> findByClienteId(UUID clienteId);

    List<OrdemServico> findByStatus(StatusOS status);
}
