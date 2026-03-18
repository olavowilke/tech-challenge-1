package br.com.oficina.veiculo.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeiculoRepository {
    Optional<Veiculo> findById(UUID id);
    Optional<Veiculo> findByPlaca(String placa);
    List<Veiculo> findByClienteId(UUID clienteId);
    boolean existsByPlaca(String placa);
    Veiculo save(Veiculo veiculo);
    void deleteById(UUID id);
    boolean existsById(UUID id);
}
