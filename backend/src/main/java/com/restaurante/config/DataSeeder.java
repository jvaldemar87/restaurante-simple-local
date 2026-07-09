package com.restaurante.config;

import com.restaurante.model.Usuario;
import com.restaurante.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String USERS_FILE = "usuarios";

    public DataSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        loadUsersFromFile();
    }

    private void loadUsersFromFile() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(USERS_FILE);
        if (inputStream == null) {
            System.out.println(">>> [DataSeeder] Archivo '" + USERS_FILE + "' no encontrado en resources. No se cargaron usuarios.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int loaded = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 3) {
                    System.out.println(">>> [DataSeeder] Linea ignorada (formato invalido): " + line);
                    continue;
                }

                String username = parts[0].trim();
                String password = parts[1].trim();
                String rol = parts[2].trim().toUpperCase();

                if (!rol.equals("ADMIN") && !rol.equals("MESERO") && !rol.equals("CAJERO")) {
                    System.out.println(">>> [DataSeeder] Linea ignorada (rol invalido): " + line);
                    continue;
                }

                Optional<Usuario> existing = usuarioRepository.findByUsername(username);
                Usuario usuario;
                if (existing.isPresent()) {
                    usuario = existing.get();
                } else {
                    usuario = new Usuario();
                    usuario.setNombre(username);
                    usuario.setActivo(true);
                }

                usuario.setUsername(username);
                usuario.setPassword(passwordEncoder.encode(password));
                usuario.setRol(rol);
                usuarioRepository.save(usuario);
                loaded++;
            }

            System.out.println(">>> [DataSeeder] " + loaded + " usuarios cargados desde archivo '" + USERS_FILE + "'");
        } catch (Exception e) {
            System.err.println(">>> [DataSeeder] Error al leer archivo '" + USERS_FILE + "': " + e.getMessage());
        }
    }
}
