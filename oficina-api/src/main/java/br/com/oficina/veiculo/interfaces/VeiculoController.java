package br.com.oficina.veiculo.interfaces;

import br.com.oficina.shared.domain.ApiError;
import br.com.oficina.veiculo.application.AtualizarVeiculoCommand;
import br.com.oficina.veiculo.application.CadastrarVeiculoCommand;
import br.com.oficina.veiculo.application.VeiculoService;
import br.com.oficina.veiculo.domain.Veiculo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/veiculos")
@Tag(name = "Veículos", description = "Cadastro e gestão de veículos vinculados a clientes")
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @Operation(summary = "Cadastrar veículo", description = "Cria um veículo vinculado a um cliente existente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Veículo criado",
                    content = @Content(schema = @Schema(implementation = VeiculoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Placa inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<VeiculoResponse> cadastrar(@Valid @RequestBody CadastrarVeiculoRequest request) {
        CadastrarVeiculoCommand command = new CadastrarVeiculoCommand(
                request.clienteId(), request.placa(), request.marca(),
                request.modelo(), request.ano(), request.cor());
        Veiculo veiculo = veiculoService.cadastrar(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(VeiculoResponse.from(veiculo));
    }

    @Operation(summary = "Listar veículos por cliente", description = "Retorna todos os veículos de um cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de veículos",
                    content = @Content(schema = @Schema(implementation = VeiculoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<VeiculoResponse>> listarPorCliente(@PathVariable UUID clienteId) {
        return ResponseEntity.ok(
                veiculoService.listarPorCliente(clienteId).stream().map(VeiculoResponse::from).toList());
    }

    @Operation(summary = "Buscar veículo por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo encontrado",
                    content = @Content(schema = @Schema(implementation = VeiculoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(VeiculoResponse.from(veiculoService.buscarPorId(id)));
    }

    @Operation(summary = "Atualizar veículo", description = "Atualiza marca, modelo, ano e cor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo atualizado",
                    content = @Content(schema = @Schema(implementation = VeiculoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponse> atualizar(@PathVariable UUID id,
                                                      @Valid @RequestBody AtualizarVeiculoRequest request) {
        AtualizarVeiculoCommand command = new AtualizarVeiculoCommand(
                request.marca(), request.modelo(), request.ano(), request.cor());
        return ResponseEntity.ok(VeiculoResponse.from(veiculoService.atualizar(id, command)));
    }

    @Operation(summary = "Remover veículo", description = "Remove o veículo do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Veículo removido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        veiculoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
