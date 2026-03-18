package br.com.oficina.cliente.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository {
    Optional<Cliente> findById(UUID id);
    Optional<Cliente> findByDocumento(String numero);
    boolean existsByDocumento(String numero);
    List<Cliente> findAll();
    Cliente save(Cliente cliente);
    void deleteById(UUID id);
    boolean existsById(UUID id);
}
