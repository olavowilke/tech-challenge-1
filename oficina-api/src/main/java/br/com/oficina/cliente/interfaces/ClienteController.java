package br.com.oficina.cliente.interfaces;

import br.com.oficina.cliente.application.AtualizarClienteCommand;
import br.com.oficina.cliente.application.CadastrarClienteCommand;
import br.com.oficina.cliente.application.ClienteService;
import br.com.oficina.cliente.domain.Cliente;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody CadastrarClienteRequest request) {
        CadastrarClienteCommand command = new CadastrarClienteCommand(
                request.nome(),
                request.email(),
                request.telefone(),
                request.documento(),
                request.tipoDocumento()
        );
        Cliente cliente = clienteService.cadastrar(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ClienteResponse.from(cliente));
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar() {
        List<ClienteResponse> responses = clienteService.listar().stream()
                .map(ClienteResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable UUID id) {
        Cliente cliente = clienteService.buscarPorId(id);
        return ResponseEntity.ok(ClienteResponse.from(cliente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(@PathVariable UUID id,
                                                      @Valid @RequestBody AtualizarClienteRequest request) {
        AtualizarClienteCommand command = new AtualizarClienteCommand(
                request.nome(),
                request.email(),
                request.telefone()
        );
        Cliente cliente = clienteService.atualizar(id, command);
        return ResponseEntity.ok(ClienteResponse.from(cliente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        clienteService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
