package br.com.oficina.ordemservico.interfaces;

import br.com.oficina.ordemservico.application.AdicionarPecaCommand;
import br.com.oficina.ordemservico.application.AdicionarServicoCommand;
import br.com.oficina.ordemservico.application.CriarOrdemServicoCommand;
import br.com.oficina.ordemservico.application.OrdemServicoService;
import br.com.oficina.ordemservico.domain.OrdemServico;
import br.com.oficina.ordemservico.domain.StatusOS;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;

@RestController
@Tag(name = "Ordens de Serviço")
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    public OrdemServicoController(OrdemServicoService ordemServicoService) {
        this.ordemServicoService = ordemServicoService;
    }

    @PostMapping("/ordens-servico")
    public ResponseEntity<OrdemServicoResponse> criar(@Valid @RequestBody CriarOrdemServicoRequest request) {
        CriarOrdemServicoCommand command = new CriarOrdemServicoCommand(request.clienteId(), request.veiculoId(), request.observacoes());
        OrdemServico os = ordemServicoService.criar(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrdemServicoResponse.from(os));
    }

    @GetMapping("/ordens-servico")
    public ResponseEntity<List<OrdemServicoResponse>> listar(
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) StatusOS status) {
        List<OrdemServico> lista;
        if (clienteId != null) {
            lista = ordemServicoService.listarPorCliente(clienteId);
        } else if (status != null) {
            lista = ordemServicoService.listarPorStatus(status);
        } else {
            lista = ordemServicoService.listar();
        }
        return ResponseEntity.ok(lista.stream().map(OrdemServicoResponse::from).toList());
    }

    @GetMapping("/ordens-servico/{id}")
    public ResponseEntity<OrdemServicoResponse> buscarPorId(@PathVariable UUID id) {
        OrdemServico os = ordemServicoService.buscarPorId(id);
        return ResponseEntity.ok(OrdemServicoResponse.from(os));
    }

    // Endpoint público para consulta de status (sem autenticação - configurado no SecurityConfig)
    @GetMapping("/public/ordens-servico/{id}/status")
    public ResponseEntity<OrdemServicoResponse> consultarStatus(@PathVariable UUID id) {
        OrdemServico os = ordemServicoService.buscarPorId(id);
        return ResponseEntity.ok(OrdemServicoResponse.from(os));
    }

    @PostMapping("/ordens-servico/{id}/servicos")
    public ResponseEntity<OrdemServicoResponse> adicionarServico(@PathVariable UUID id,
                                                                  @Valid @RequestBody AdicionarServicoRequest request) {
        AdicionarServicoCommand command = new AdicionarServicoCommand(id, request.servicoId());
        OrdemServico os = ordemServicoService.adicionarServico(command);
        return ResponseEntity.ok(OrdemServicoResponse.from(os));
    }

    @DeleteMapping("/ordens-servico/{id}/servicos/{itemServicoId}")
    public ResponseEntity<OrdemServicoResponse> removerServico(@PathVariable UUID id,
                                                                @PathVariable UUID itemServicoId) {
        OrdemServico os = ordemServicoService.removerServico(id, itemServicoId);
        return ResponseEntity.ok(OrdemServicoResponse.from(os));
    }

    @PostMapping("/ordens-servico/{id}/pecas")
    public ResponseEntity<OrdemServicoResponse> adicionarPeca(@PathVariable UUID id,
                                                               @Valid @RequestBody AdicionarPecaRequest request) {
        AdicionarPecaCommand command = new AdicionarPecaCommand(id, request.pecaId(), request.quantidade());
        OrdemServico os = ordemServicoService.adicionarPeca(command);
        return ResponseEntity.ok(OrdemServicoResponse.from(os));
    }

    @DeleteMapping("/ordens-servico/{id}/pecas/{itemPecaId}")
    public ResponseEntity<OrdemServicoResponse> removerPeca(@PathVariable UUID id,
                                                             @PathVariable UUID itemPecaId) {
        OrdemServico os = ordemServicoService.removerPeca(id, itemPecaId);
        return ResponseEntity.ok(OrdemServicoResponse.from(os));
    }

    @PatchMapping("/ordens-servico/{id}/status")
    public ResponseEntity<OrdemServicoResponse> avancarStatus(@PathVariable UUID id,
                                                               @Valid @RequestBody AvancarStatusRequest request) {
        OrdemServico os = ordemServicoService.avancarStatus(id, request.novoStatus());
        return ResponseEntity.ok(OrdemServicoResponse.from(os));
    }

    @PostMapping("/ordens-servico/{id}/aprovar-orcamento")
    public ResponseEntity<OrdemServicoResponse> aprovarOrcamento(@PathVariable UUID id) {
        OrdemServico os = ordemServicoService.aprovarOrcamento(id);
        return ResponseEntity.ok(OrdemServicoResponse.from(os));
    }

    @PostMapping("/ordens-servico/{id}/recusar-orcamento")
    public ResponseEntity<OrdemServicoResponse> recusarOrcamento(@PathVariable UUID id) {
        OrdemServico os = ordemServicoService.recusarOrcamento(id);
        return ResponseEntity.ok(OrdemServicoResponse.from(os));
    }

    @GetMapping("/ordens-servico/monitoramento/tempo-medio-execucao")
    public ResponseEntity<TempoMedioExecucaoResponse> monitorarTempoMedioExecucao() {
        OptionalDouble media = ordemServicoService.monitorarTempoMedioExecucao();
        TempoMedioExecucaoResponse response = media.isPresent()
                ? TempoMedioExecucaoResponse.comValor(media.getAsDouble())
                : TempoMedioExecucaoResponse.semDados();
        return ResponseEntity.ok(response);
    }
}
