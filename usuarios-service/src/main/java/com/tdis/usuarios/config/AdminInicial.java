package com.tdis.usuarios.config;

import com.tdis.common.enums.TipoUsuario;
import com.tdis.usuarios.entity.Usuario;
import com.tdis.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * En producción no se cargan usuarios de prueba. Si al arrancar no existe ningún
 * administrador, se crea uno con ADMIN_EMAIL y ADMIN_PASSWORD.
 * Después de creado, estas variables ya no se usan (puedes quitarlas).
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class AdminInicial implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${tdis.admin.email:}")
    private String email;

    @Value("${tdis.admin.password:}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (!usuarioRepository.findByTipoUsuario(TipoUsuario.ADMINISTRADOR).isEmpty()) {
            return; // ya hay administrador
        }
        if (email.isBlank() || password.isBlank()) {
            log.warn("No existe ningun administrador. Define ADMIN_EMAIL y ADMIN_PASSWORD para crearlo.");
            return;
        }
        if (password.length() < 12) {
            throw new IllegalStateException("ADMIN_PASSWORD debe tener al menos 12 caracteres");
        }

        Usuario admin = new Usuario();
        admin.setEmail(email.trim());
        admin.setPassword(passwordEncoder.encode(password));
        admin.setNombre("Administrador");
        admin.setApellidos("TDIs");
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        admin.setActivo(true);
        usuarioRepository.save(admin);
        log.info("Administrador inicial creado: {}", email);
    }
}
