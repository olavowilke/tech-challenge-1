package br.com.oficina.auth.application;

import br.com.oficina.auth.domain.Usuario;
import br.com.oficina.auth.domain.UsuarioRepository;
import br.com.oficina.auth.infrastructure.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public Usuario register(RegisterCommand command) {
        if (usuarioRepository.existsByUsername(command.username())) {
            throw new IllegalArgumentException("Username já está em uso: " + command.username());
        }
        String hash = passwordEncoder.encode(command.password());
        Usuario usuario = Usuario.novo(command.username(), hash, command.role());
        return usuarioRepository.save(usuario);
    }

    public String login(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return jwtService.generateToken(usuario.getUsername(), usuario.getRole().name());
    }
}
