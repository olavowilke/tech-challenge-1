package br.com.oficina.veiculo.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeiculoJpaRepository extends JpaRepository<VeiculoEntity, UUID> {
    Optional<VeiculoEntity> findByPlaca(String placa);
    boolean existsByPlaca(String placa);
    List<VeiculoEntity> findByClienteId(UUID clienteId);
}
