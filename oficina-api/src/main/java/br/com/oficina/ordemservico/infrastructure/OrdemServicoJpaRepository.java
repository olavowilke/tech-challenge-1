package br.com.oficina.ordemservico.infrastructure;

import br.com.oficina.ordemservico.domain.StatusOS;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoEntity, UUID> {

    List<OrdemServicoEntity> findByClienteId(UUID clienteId);

    List<OrdemServicoEntity> findByStatus(StatusOS status);
}
