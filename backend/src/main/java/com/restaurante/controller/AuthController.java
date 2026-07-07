package com.restaurante.controller;

import com.restaurante.dto.LoginRequest;
import com.restaurante.dto.LoginResponse;
import com.restaurante.model.Usuario;
import com.restaurante.repository.UsuarioRepository;
import com.restaurante.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRol(), usuario.getNombre());

        return ResponseEntity.ok(new LoginResponse(token, usuario.getRol(), usuario.getNombre()));
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) auth.getPrincipal();
        Usuario usuario = usuarioRepository.findByUsername(username).orElseThrow();
        return ResponseEntity.ok(new LoginResponse(null, usuario.getRol(), usuario.getNombre()));
    }
}
