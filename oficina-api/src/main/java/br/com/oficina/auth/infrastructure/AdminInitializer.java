package br.com.oficina.auth.infrastructure;

import br.com.oficina.auth.domain.Role;
import br.com.oficina.auth.domain.Usuario;
import br.com.oficina.auth.domain.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = Usuario.novo("admin", passwordEncoder.encode("admin123"), Role.ADMIN);
            usuarioRepository.save(admin);
        }
    }
}
