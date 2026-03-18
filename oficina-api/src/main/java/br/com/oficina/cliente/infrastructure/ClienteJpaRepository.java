package br.com.oficina.cliente.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, UUID> {
    Optional<ClienteEntity> findByDocumento(String documento);
    boolean existsByDocumento(String documento);
}
