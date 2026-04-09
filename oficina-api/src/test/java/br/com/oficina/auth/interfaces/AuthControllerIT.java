package br.com.oficina.auth.interfaces;

import br.com.oficina.auth.domain.Role;
import br.com.oficina.shared.BaseControllerIT;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerIT extends BaseControllerIT {

    @Test
    void shouldReturn201WhenRegisteringNewUser() {
        RegisterRequest request = new RegisterRequest("mecanico1", "senha123", Role.MECANICO);
        ResponseEntity<UsuarioResponse> response = restTemplate.postForEntity(
                "/auth/register", request, UsuarioResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("mecanico1", response.getBody().username());
        assertEquals(Role.MECANICO, response.getBody().role());
    }

    @Test
    void shouldReturn200AndTokenWhenLoginWithAdminCredentials() {
        LoginRequest request = new LoginRequest("admin", "admin123");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/auth/login", request, AuthResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().token());
        assertFalse(response.getBody().token().isBlank());
    }

    @Test
    void shouldReturn400WhenLoginWithWrongPassword() {
        LoginRequest request = new LoginRequest("admin", "senhaerrada");
        ResponseEntity<Object> response = restTemplate.postForEntity(
                "/auth/login", request, Object.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void shouldReturn400WhenRegisteringDuplicateUsername() {
        RegisterRequest request = new RegisterRequest("admin", "qualquersenha", Role.ADMIN);
        ResponseEntity<Object> response = restTemplate.postForEntity(
                "/auth/register", request, Object.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldReturn401WhenAccessingProtectedRouteWithoutToken() {
        ResponseEntity<Object> response = restTemplate.getForEntity("/clientes", Object.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void shouldReturn200WhenAccessingProtectedRouteWithValidToken() {
        // Primeiro faz login
        LoginRequest loginRequest = new LoginRequest("admin", "admin123");
        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                "/auth/login", loginRequest, AuthResponse.class);
        String token = loginResponse.getBody().token();

        // Usa o token para acessar rota protegida
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                "/clientes", HttpMethod.GET, entity, Object.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldReturn200WhenAccessingPublicStatusEndpointWithoutToken() {
        // A rota /public/** deve ser acessível sem token
        // Criamos uma OS primeiro com token
        LoginRequest loginRequest = new LoginRequest("admin", "admin123");
        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                "/auth/login", loginRequest, AuthResponse.class);
        String token = loginResponse.getBody().token();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        // Cria cliente
        var clienteBody = new java.util.HashMap<String, Object>();
        clienteBody.put("nome", "João");
        clienteBody.put("email", "joao@test.com");
        clienteBody.put("telefone", "11999999999");
        clienteBody.put("documento", "529.982.247-25");
        clienteBody.put("tipoDocumento", "CPF");
        ResponseEntity<java.util.Map> clienteRes = restTemplate.exchange(
                "/clientes", HttpMethod.POST,
                new HttpEntity<>(clienteBody, headers), java.util.Map.class);
        java.util.UUID clienteId = java.util.UUID.fromString((String) clienteRes.getBody().get("id"));

        // Cria veículo
        var veiculoBody = new java.util.HashMap<String, Object>();
        veiculoBody.put("clienteId", clienteId.toString());
        veiculoBody.put("placa", "XYZ-9999");
        veiculoBody.put("marca", "Honda");
        veiculoBody.put("modelo", "Civic");
        veiculoBody.put("ano", 2022);
        veiculoBody.put("cor", "Preto");
        ResponseEntity<java.util.Map> veiculoRes = restTemplate.exchange(
                "/veiculos", HttpMethod.POST,
                new HttpEntity<>(veiculoBody, headers), java.util.Map.class);
        java.util.UUID veiculoId = java.util.UUID.fromString((String) veiculoRes.getBody().get("id"));

        // Cria OS
        var osBody = new java.util.HashMap<String, Object>();
        osBody.put("clienteId", clienteId.toString());
        osBody.put("veiculoId", veiculoId.toString());
        ResponseEntity<java.util.Map> osRes = restTemplate.exchange(
                "/ordens-servico", HttpMethod.POST,
                new HttpEntity<>(osBody, headers), java.util.Map.class);
        String osId = (String) osRes.getBody().get("id");

        // Acessa o status público SEM token
        ResponseEntity<Object> publicResponse = restTemplate.getForEntity(
                "/public/ordens-servico/" + osId + "/status", Object.class);
        assertEquals(HttpStatus.OK, publicResponse.getStatusCode());
    }
}
