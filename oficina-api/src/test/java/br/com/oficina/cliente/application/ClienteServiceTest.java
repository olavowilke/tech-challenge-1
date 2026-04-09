package br.com.oficina.cliente.application;

import br.com.oficina.cliente.domain.Cliente;
import br.com.oficina.cliente.domain.ClienteRepository;
import br.com.oficina.cliente.domain.Documento;
import br.com.oficina.cliente.domain.TipoDocumento;
import br.com.oficina.shared.domain.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private static final String VALID_CPF = "52998224725";

    private CadastrarClienteCommand buildCommand() {
        return new CadastrarClienteCommand(
                "João Silva",
                "joao@example.com",
                "11999999999",
                VALID_CPF,
                TipoDocumento.CPF
        );
    }

    private Cliente buildCliente() {
        Documento documento = new Documento(VALID_CPF, TipoDocumento.CPF);
        return Cliente.novo("João Silva", "joao@example.com", "11999999999", documento);
    }

    @Test
    void shouldCadastrarAndSaveCliente() {
        CadastrarClienteCommand command = buildCommand();
        when(clienteRepository.existsByDocumento(VALID_CPF)).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente result = clienteService.cadastrar(command);

        assertNotNull(result);
        assertEquals("João Silva", result.getNome());
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void shouldThrowWhenDocumentoAlreadyExists() {
        CadastrarClienteCommand command = buildCommand();
        when(clienteRepository.existsByDocumento(VALID_CPF)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> clienteService.cadastrar(command));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void shouldAtualizarAndSaveCliente() {
        UUID id = UUID.randomUUID();
        Cliente cliente = buildCliente();
        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        AtualizarClienteCommand command = new AtualizarClienteCommand("Novo Nome", "novo@example.com", "11888888888");
        Cliente result = clienteService.atualizar(id, command);

        assertEquals("Novo Nome", result.getNome());
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void shouldThrowWhenAtualizarAndClienteNotFound() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        AtualizarClienteCommand command = new AtualizarClienteCommand("Novo Nome", "novo@example.com", null);
        assertThrows(RecursoNaoEncontradoException.class, () -> clienteService.atualizar(id, command));
    }

    @Test
    void shouldDesativarClienteOnRemover() {
        UUID id = UUID.randomUUID();
        Cliente cliente = buildCliente();
        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        clienteService.remover(id);

        assertFalse(cliente.isAtivo());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void shouldThrowWhenRemoverAndClienteNotFound() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> clienteService.remover(id));
    }

    @Test
    void listarDeveRetornarApenasClientesAtivos() {
        Cliente c1 = buildCliente();
        when(clienteRepository.findAllAtivos()).thenReturn(List.of(c1));

        List<Cliente> result = clienteService.listar();

        assertEquals(1, result.size());
        verify(clienteRepository).findAllAtivos();
        verify(clienteRepository, never()).findAll();
    }
}
