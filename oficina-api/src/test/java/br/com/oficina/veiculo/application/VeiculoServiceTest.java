package br.com.oficina.veiculo.application;

import br.com.oficina.cliente.domain.ClienteRepository;
import br.com.oficina.shared.domain.RecursoNaoEncontradoException;
import br.com.oficina.veiculo.domain.Placa;
import br.com.oficina.veiculo.domain.Veiculo;
import br.com.oficina.veiculo.domain.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private VeiculoService veiculoService;

    private UUID clienteId;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
    }

    private Veiculo buildVeiculo() {
        return Veiculo.novo(clienteId, new Placa("ABC1234"), "Toyota", "Corolla", 2020, "Prata");
    }

    @Test
    void deveCadastrarVeiculoComSucesso() {
        CadastrarVeiculoCommand command = new CadastrarVeiculoCommand(
                clienteId, "ABC-1234", "Toyota", "Corolla", 2020, "Prata");
        when(clienteRepository.existsById(clienteId)).thenReturn(true);
        // VeiculoService normalizes the plate (strips hyphens, uppercases) BEFORE
        // calling existsByPlaca, so the stub must match the normalized value.
        when(veiculoRepository.existsByPlaca("ABC1234")).thenReturn(false);
        when(veiculoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Veiculo result = veiculoService.cadastrar(command);

        assertThat(result).isNotNull();
        assertThat(result.getMarca()).isEqualTo("Toyota");
        verify(veiculoRepository).save(any());
    }

    @Test
    void deveLancarExcecaoAoCadastrarSeClienteNaoExiste() {
        CadastrarVeiculoCommand command = new CadastrarVeiculoCommand(
                clienteId, "ABC-1234", "Toyota", "Corolla", 2020, "Prata");
        when(clienteRepository.existsById(clienteId)).thenReturn(false);

        assertThatThrownBy(() -> veiculoService.cadastrar(command))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoCadastrarComPlacaDuplicada() {
        CadastrarVeiculoCommand command = new CadastrarVeiculoCommand(
                clienteId, "ABC-1234", "Toyota", "Corolla", 2020, "Prata");
        when(clienteRepository.existsById(clienteId)).thenReturn(true);
        when(veiculoRepository.existsByPlaca("ABC1234")).thenReturn(true);

        assertThatThrownBy(() -> veiculoService.cadastrar(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("placa");
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveAtualizarVeiculoComSucesso() {
        UUID id = UUID.randomUUID();
        Veiculo veiculo = buildVeiculo();
        when(veiculoRepository.findById(id)).thenReturn(Optional.of(veiculo));
        when(veiculoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AtualizarVeiculoCommand command = new AtualizarVeiculoCommand("Honda", "Civic", 2022, "Preto");
        Veiculo result = veiculoService.atualizar(id, command);

        assertThat(result.getMarca()).isEqualTo("Honda");
        assertThat(result.getModelo()).isEqualTo("Civic");
        verify(veiculoRepository).save(any());
    }

    @Test
    void deveLancarExcecaoAoAtualizarVeiculoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(veiculoRepository.findById(id)).thenReturn(Optional.empty());

        AtualizarVeiculoCommand command = new AtualizarVeiculoCommand("Honda", "Civic", 2022, "Preto");
        assertThatThrownBy(() -> veiculoService.atualizar(id, command))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveBuscarVeiculoPorId() {
        UUID id = UUID.randomUUID();
        Veiculo veiculo = buildVeiculo();
        when(veiculoRepository.findById(id)).thenReturn(Optional.of(veiculo));

        Veiculo result = veiculoService.buscarPorId(id);

        assertThat(result).isEqualTo(veiculo);
    }

    @Test
    void deveLancarExcecaoAoBuscarVeiculoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(veiculoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> veiculoService.buscarPorId(id))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveListarVeiculosPorCliente() {
        Veiculo veiculo = buildVeiculo();
        when(clienteRepository.existsById(clienteId)).thenReturn(true);
        when(veiculoRepository.findByClienteId(clienteId)).thenReturn(List.of(veiculo));

        List<Veiculo> result = veiculoService.listarPorCliente(clienteId);

        assertThat(result).hasSize(1);
        verify(veiculoRepository).findByClienteId(clienteId);
    }

    @Test
    void deveLancarExcecaoAoListarVeiculosDeClienteNaoEncontrado() {
        when(clienteRepository.existsById(clienteId)).thenReturn(false);

        assertThatThrownBy(() -> veiculoService.listarPorCliente(clienteId))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(veiculoRepository, never()).findByClienteId(any());
    }

    @Test
    void deveRemoverVeiculoExistente() {
        UUID id = UUID.randomUUID();
        when(veiculoRepository.existsById(id)).thenReturn(true);

        veiculoService.remover(id);

        verify(veiculoRepository).deleteById(id);
    }

    @Test
    void deveLancarExcecaoAoRemoverVeiculoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(veiculoRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> veiculoService.remover(id))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(veiculoRepository, never()).deleteById(any());
    }
}
