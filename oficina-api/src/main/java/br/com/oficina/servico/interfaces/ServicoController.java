package br.com.oficina.servico.interfaces;

import br.com.oficina.servico.application.AtualizarServicoCommand;
import br.com.oficina.servico.application.CadastrarServicoCommand;
import br.com.oficina.servico.application.ServicoService;
import br.com.oficina.servico.domain.Servico;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/servicos")
@Tag(name = "Serviços")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @PostMapping
    public ResponseEntity<ServicoResponse> cadastrar(@Valid @RequestBody CadastrarServicoRequest request) {
        CadastrarServicoCommand command = new CadastrarServicoCommand(
                request.nome(),
                request.descricao(),
                request.preco(),
                request.tempoEstimadoMinutos()
        );
        Servico servico = servicoService.cadastrar(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ServicoResponse.from(servico));
    }

    @GetMapping
    public ResponseEntity<List<ServicoResponse>> listar() {
        List<ServicoResponse> responses = servicoService.listar().stream()
                .map(ServicoResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponse> buscarPorId(@PathVariable UUID id) {
        Servico servico = servicoService.buscarPorId(id);
        return ResponseEntity.ok(ServicoResponse.from(servico));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponse> atualizar(@PathVariable UUID id,
                                                      @Valid @RequestBody AtualizarServicoRequest request) {
        AtualizarServicoCommand command = new AtualizarServicoCommand(
                request.nome(),
                request.descricao(),
                request.preco(),
                request.tempoEstimadoMinutos()
        );
        Servico servico = servicoService.atualizar(id, command);
        return ResponseEntity.ok(ServicoResponse.from(servico));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        servicoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
