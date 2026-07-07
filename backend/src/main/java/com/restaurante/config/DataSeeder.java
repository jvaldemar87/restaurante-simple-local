package com.restaurante.config;

import com.restaurante.model.Usuario;
import com.restaurante.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol("ADMIN");
            admin.setActivo(true);
            usuarioRepository.save(admin);

            Usuario mesero = new Usuario();
            mesero.setNombre("Mesero 1");
            mesero.setUsername("mesero");
            mesero.setPassword(passwordEncoder.encode("mesero123"));
            mesero.setRol("MESERO");
            mesero.setActivo(true);
            usuarioRepository.save(mesero);

            Usuario cajero = new Usuario();
            cajero.setNombre("Cajero 1");
            cajero.setUsername("cajero");
            cajero.setPassword(passwordEncoder.encode("cajero123"));
            cajero.setRol("CAJERO");
            cajero.setActivo(true);
            usuarioRepository.save(cajero);

            System.out.println(">>> Usuarios por defecto creados: admin/admin123, mesero/mesero123, cajero/cajero123");
        }
    }
}
