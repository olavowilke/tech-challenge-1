package br.com.oficina.ordemservico.infrastructure;

import br.com.oficina.ordemservico.domain.StatusOS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoEntity, UUID> {

    List<OrdemServicoEntity> findByClienteId(UUID clienteId);

    List<OrdemServicoEntity> findByStatus(StatusOS status);

    @Query(value = """
            SELECT AVG(EXTRACT(EPOCH FROM (fim_execucao - inicio_execucao)) / 60)
            FROM ordens_servico
            WHERE status = 'FINALIZADA'
              AND inicio_execucao IS NOT NULL
              AND fim_execucao IS NOT NULL
            """, nativeQuery = true)
    Double findAverageExecutionMinutes();
}
