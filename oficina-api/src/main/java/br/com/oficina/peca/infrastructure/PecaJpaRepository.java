package br.com.oficina.peca.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PecaJpaRepository extends JpaRepository<PecaEntity, UUID> {
    List<PecaEntity> findByAtivoTrue();
}
