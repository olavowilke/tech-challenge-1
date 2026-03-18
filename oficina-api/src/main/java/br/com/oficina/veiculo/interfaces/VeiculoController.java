package br.com.oficina.veiculo.interfaces;

import br.com.oficina.veiculo.application.AtualizarVeiculoCommand;
import br.com.oficina.veiculo.application.CadastrarVeiculoCommand;
import br.com.oficina.veiculo.application.VeiculoService;
import br.com.oficina.veiculo.domain.Veiculo;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/veiculos")
@Tag(name = "Veículos")
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @PostMapping
    public ResponseEntity<VeiculoResponse> cadastrar(@Valid @RequestBody CadastrarVeiculoRequest request) {
        CadastrarVeiculoCommand command = new CadastrarVeiculoCommand(
                request.clienteId(),
                request.placa(),
                request.marca(),
                request.modelo(),
                request.ano(),
                request.cor()
        );
        Veiculo veiculo = veiculoService.cadastrar(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(VeiculoResponse.from(veiculo));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<VeiculoResponse>> listarPorCliente(@PathVariable UUID clienteId) {
        List<VeiculoResponse> responses = veiculoService.listarPorCliente(clienteId).stream()
                .map(VeiculoResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponse> buscarPorId(@PathVariable UUID id) {
        Veiculo veiculo = veiculoService.buscarPorId(id);
        return ResponseEntity.ok(VeiculoResponse.from(veiculo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponse> atualizar(@PathVariable UUID id,
                                                      @Valid @RequestBody AtualizarVeiculoRequest request) {
        AtualizarVeiculoCommand command = new AtualizarVeiculoCommand(
                request.marca(),
                request.modelo(),
                request.ano(),
                request.cor()
        );
        Veiculo veiculo = veiculoService.atualizar(id, command);
        return ResponseEntity.ok(VeiculoResponse.from(veiculo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        veiculoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
