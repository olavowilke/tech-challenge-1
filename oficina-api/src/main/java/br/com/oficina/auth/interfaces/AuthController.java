package br.com.oficina.auth.interfaces;

import br.com.oficina.auth.application.AuthService;
import br.com.oficina.auth.application.RegisterCommand;
import br.com.oficina.auth.domain.Usuario;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;
    private final long expirationMs;

    public AuthController(AuthService authService,
                          @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.authService = authService;
        this.expirationMs = expirationMs;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterCommand command = new RegisterCommand(request.username(), request.password(), request.role());
        Usuario usuario = authService.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.from(usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.username(), request.password());
        return ResponseEntity.ok(new AuthResponse(token, request.username(), null, expirationMs));
    }
}
