package br.com.oficina.cliente.interfaces;

import br.com.oficina.cliente.domain.TipoDocumento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class ClienteControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("oficina_test")
            .withUsername("oficina")
            .withPassword("oficina");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String VALID_CPF = "52998224725";
    private static final String BASE_URL = "/clientes";

    private CadastrarClienteRequest buildRequest(String cpf) {
        return new CadastrarClienteRequest(
                "João Silva",
                "joao@example.com",
                "11999999999",
                cpf,
                TipoDocumento.CPF
        );
    }

    @Test
    void shouldReturn201WhenCreatingClienteWithValidRequest() {
        CadastrarClienteRequest request = buildRequest(VALID_CPF);

        ResponseEntity<ClienteResponse> response = restTemplate.postForEntity(BASE_URL, request, ClienteResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("João Silva", response.getBody().nome());
    }

    @Test
    void shouldReturn422WhenCreatingClienteWithMissingNome() {
        CadastrarClienteRequest request = new CadastrarClienteRequest(
                "",
                "joao@example.com",
                "11999999999",
                "01234567890",
                TipoDocumento.CPF
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(BASE_URL, request, Map.class);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    void shouldReturn400WhenCreatingClienteWithInvalidCPF() {
        CadastrarClienteRequest request = new CadastrarClienteRequest(
                "João Silva",
                "joao@example.com",
                "11999999999",
                "12345678900",
                TipoDocumento.CPF
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(BASE_URL, request, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldReturn200WhenListingClientes() {
        ResponseEntity<ClienteResponse[]> response = restTemplate.getForEntity(BASE_URL, ClienteResponse[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldReturn200WhenGettingClienteByIdAfterCreating() {
        // Use a unique CPF for this test
        String uniqueCpf = "39053344705";
        CadastrarClienteRequest request = new CadastrarClienteRequest(
                "Maria Souza",
                "maria@example.com",
                "11888888888",
                uniqueCpf,
                TipoDocumento.CPF
        );

        ResponseEntity<ClienteResponse> created = restTemplate.postForEntity(BASE_URL, request, ClienteResponse.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        UUID id = created.getBody().id();

        ResponseEntity<ClienteResponse> response = restTemplate.getForEntity(BASE_URL + "/" + id, ClienteResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(id, response.getBody().id());
    }

    @Test
    void shouldReturn404WhenGettingUnknownCliente() {
        UUID unknownId = UUID.randomUUID();
        ResponseEntity<Map> response = restTemplate.getForEntity(BASE_URL + "/" + unknownId, Map.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturn200WhenUpdatingCliente() {
        // Use a unique CPF for this test
        String uniqueCpf = "71428793860";
        CadastrarClienteRequest createRequest = new CadastrarClienteRequest(
                "Carlos Lima",
                "carlos@example.com",
                "11777777777",
                uniqueCpf,
                TipoDocumento.CPF
        );

        ResponseEntity<ClienteResponse> created = restTemplate.postForEntity(BASE_URL, createRequest, ClienteResponse.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        UUID id = created.getBody().id();

        AtualizarClienteRequest updateRequest = new AtualizarClienteRequest(
                "Carlos Lima Atualizado",
                "carlos.novo@example.com",
                "11666666666"
        );

        HttpEntity<AtualizarClienteRequest> entity = new HttpEntity<>(updateRequest);
        ResponseEntity<ClienteResponse> response = restTemplate.exchange(
                BASE_URL + "/" + id, HttpMethod.PUT, entity, ClienteResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Carlos Lima Atualizado", response.getBody().nome());
    }

    @Test
    void shouldReturn204WhenDeletingCliente() {
        // Use a unique CPF for this test
        String uniqueCpf = "87654321007";
        CadastrarClienteRequest createRequest = new CadastrarClienteRequest(
                "Ana Costa",
                "ana@example.com",
                "11555555555",
                uniqueCpf,
                TipoDocumento.CPF
        );

        ResponseEntity<ClienteResponse> created = restTemplate.postForEntity(BASE_URL, createRequest, ClienteResponse.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        UUID id = created.getBody().id();

        ResponseEntity<Void> response = restTemplate.exchange(
                BASE_URL + "/" + id, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
