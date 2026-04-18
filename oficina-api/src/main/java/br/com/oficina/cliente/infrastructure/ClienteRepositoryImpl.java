package br.com.oficina.cliente.infrastructure;

import br.com.oficina.cliente.domain.Cliente;
import br.com.oficina.cliente.domain.ClienteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ClienteRepositoryImpl implements ClienteRepository {

    private final ClienteJpaRepository jpaRepository;

    public ClienteRepositoryImpl(ClienteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Cliente> findById(UUID id) {
        return jpaRepository.findById(id).map(ClienteMapper::toDomain);
    }

    @Override
    public Optional<Cliente> findByDocumento(String numero) {
        return jpaRepository.findByDocumento(numero).map(ClienteMapper::toDomain);
    }

    @Override
    public boolean existsByDocumento(String numero) {
        return jpaRepository.existsByDocumento(numero);
    }

    @Override
    public List<Cliente> findAll() {
        return jpaRepository.findAll().stream()
                .map(ClienteMapper::toDomain)
                .toList();
    }

    @Override
    public List<Cliente> findAllAtivos() {
        return jpaRepository.findByAtivoTrue().stream()
                .map(ClienteMapper::toDomain)
                .toList();
    }

    @Override
    public Cliente save(Cliente cliente) {
        ClienteEntity entity = ClienteMapper.toEntity(cliente);
        ClienteEntity saved = jpaRepository.save(entity);
        return ClienteMapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
