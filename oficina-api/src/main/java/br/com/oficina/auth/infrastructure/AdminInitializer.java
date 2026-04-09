package br.com.oficina.auth.infrastructure;

import br.com.oficina.auth.domain.Role;
import br.com.oficina.auth.domain.Usuario;
import br.com.oficina.auth.domain.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminInitializer(UsuarioRepository usuarioRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${app.admin.username:admin}") String adminUsername,
                            @Value("${app.admin.password:admin123}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!usuarioRepository.existsByUsername(adminUsername)) {
            Usuario admin = Usuario.novo(adminUsername, passwordEncoder.encode(adminPassword), Role.ADMIN);
            usuarioRepository.save(admin);
        }
    }
}
