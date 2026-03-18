package br.com.oficina.veiculo.application;

import br.com.oficina.cliente.domain.ClienteRepository;
import br.com.oficina.shared.domain.RecursoNaoEncontradoException;
import br.com.oficina.veiculo.domain.Placa;
import br.com.oficina.veiculo.domain.Veiculo;
import br.com.oficina.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;

    public VeiculoService(VeiculoRepository veiculoRepository, ClienteRepository clienteRepository) {
        this.veiculoRepository = veiculoRepository;
        this.clienteRepository = clienteRepository;
    }

    public Veiculo cadastrar(CadastrarVeiculoCommand command) {
        if (!clienteRepository.existsById(command.clienteId())) {
            throw new RecursoNaoEncontradoException("Cliente", command.clienteId());
        }
        if (veiculoRepository.existsByPlaca(command.placa())) {
            throw new IllegalArgumentException("Já existe um veículo com a placa informada");
        }
        Placa placa = new Placa(command.placa());
        Veiculo veiculo = Veiculo.novo(command.clienteId(), placa, command.marca(),
                command.modelo(), command.ano(), command.cor());
        return veiculoRepository.save(veiculo);
    }

    public Veiculo atualizar(UUID id, AtualizarVeiculoCommand command) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo", id));
        veiculo.atualizar(command.marca(), command.modelo(), command.ano(), command.cor());
        return veiculoRepository.save(veiculo);
    }

    public Veiculo buscarPorId(UUID id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo", id));
    }

    public List<Veiculo> listarPorCliente(UUID clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new RecursoNaoEncontradoException("Cliente", clienteId);
        }
        return veiculoRepository.findByClienteId(clienteId);
    }

    public void remover(UUID id) {
        if (!veiculoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Veículo", id);
        }
        veiculoRepository.deleteById(id);
    }
}
