package br.com.oficina.cliente.application;

import br.com.oficina.cliente.domain.Cliente;
import br.com.oficina.cliente.domain.ClienteRepository;
import br.com.oficina.cliente.domain.Documento;
import br.com.oficina.shared.domain.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Cliente cadastrar(CadastrarClienteCommand command) {
        String numeroLimpo = command.documento().replaceAll("[^0-9]", "");
        if (clienteRepository.existsByDocumento(numeroLimpo)) {
            throw new IllegalArgumentException("Já existe um cliente com o documento informado");
        }
        Documento documento = new Documento(command.documento(), command.tipoDocumento());
        Cliente cliente = Cliente.novo(command.nome(), command.email(), command.telefone(), documento);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente atualizar(UUID id, AtualizarClienteCommand command) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
        cliente.atualizar(command.nome(), command.email(), command.telefone());
        return clienteRepository.save(cliente);
    }

    public Cliente buscarPorId(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    public List<Cliente> listar() {
        return clienteRepository.findAllAtivos();
    }

    @Transactional
    public void remover(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
        cliente.desativar();
        clienteRepository.save(cliente);
    }
}
