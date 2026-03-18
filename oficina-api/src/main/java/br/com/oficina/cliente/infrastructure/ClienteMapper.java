package br.com.oficina.cliente.infrastructure;

import br.com.oficina.cliente.domain.Cliente;
import br.com.oficina.cliente.domain.Documento;
import br.com.oficina.cliente.domain.TipoDocumento;

public class ClienteMapper {

    private ClienteMapper() {}

    public static ClienteEntity toEntity(Cliente cliente) {
        ClienteEntity entity = new ClienteEntity();
        entity.setId(cliente.getId());
        entity.setNome(cliente.getNome());
        entity.setEmail(cliente.getEmail());
        entity.setTelefone(cliente.getTelefone());
        entity.setDocumento(cliente.getDocumento().numero());
        entity.setTipoDocumento(cliente.getDocumento().tipo().name());
        entity.setAtivo(cliente.isAtivo());
        entity.setCriadoEm(cliente.getCriadoEm());
        entity.setAtualizadoEm(cliente.getAtualizadoEm());
        return entity;
    }

    public static Cliente toDomain(ClienteEntity entity) {
        TipoDocumento tipo = TipoDocumento.valueOf(entity.getTipoDocumento());
        Documento documento = new Documento(entity.getDocumento(), tipo);
        return Cliente.reconstituir(
                entity.getId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getTelefone(),
                documento,
                entity.isAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
