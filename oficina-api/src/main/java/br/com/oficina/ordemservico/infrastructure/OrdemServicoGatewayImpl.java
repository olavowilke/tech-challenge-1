package br.com.oficina.ordemservico.infrastructure;

import br.com.oficina.ordemservico.entities.OrdemServico;
import br.com.oficina.ordemservico.entities.StatusOS;
import br.com.oficina.ordemservico.gateways.OrdemServicoGateway;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

@Repository
public class OrdemServicoGatewayImpl implements OrdemServicoGateway {

    private final OrdemServicoJpaRepository jpaRepository;

    public OrdemServicoGatewayImpl(OrdemServicoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OrdemServico save(OrdemServico ordemServico) {
        OrdemServicoData data = OrdemServicoMapper.toData(ordemServico);
        return OrdemServicoMapper.toDomain(jpaRepository.save(data));
    }

    @Override
    public Optional<OrdemServico> findById(UUID id) {
        return jpaRepository.findById(id).map(OrdemServicoMapper::toDomain);
    }

    @Override
    public List<OrdemServico> findAll() {
        return jpaRepository.findAll().stream().map(OrdemServicoMapper::toDomain).toList();
    }

    @Override
    public List<OrdemServico> findByClienteId(UUID clienteId) {
        return jpaRepository.findByClienteId(clienteId).stream().map(OrdemServicoMapper::toDomain).toList();
    }

    @Override
    public List<OrdemServico> findByStatus(StatusOS status) {
        return jpaRepository.findByStatus(status).stream().map(OrdemServicoMapper::toDomain).toList();
    }

    @Override
    public OptionalDouble tempoMedioExecucaoMinutos() {
        Double media = jpaRepository.findAverageExecutionMinutes();
        return media == null ? OptionalDouble.empty() : OptionalDouble.of(media);
    }
}
