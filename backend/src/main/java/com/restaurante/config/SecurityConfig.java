package com.restaurante.config;

import com.restaurante.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .cors()
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                // Public static & auth
                .antMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/", "/index.html", "/assets/**", "/favicon.ico").permitAll()
                .antMatchers(HttpMethod.GET, "/uploads/**").permitAll()

                // Admin-only API modules
                .antMatchers("/api/reportes/ventas/**").hasRole("ADMIN")
                .antMatchers("/api/reportes/ventas-resumen/**").hasRole("ADMIN")
                .antMatchers("/api/reportes/insumos/**").hasRole("ADMIN")
                .antMatchers("/api/reportes/pagos/**").hasRole("ADMIN")
                .antMatchers("/api/insumos/**").hasRole("ADMIN")
                .antMatchers("/api/pagos/**").hasRole("ADMIN")
                .antMatchers("/api/categorias-insumo/**").hasRole("ADMIN")
                .antMatchers("/api/categorias-pago/**").hasRole("ADMIN")

                // Ticket/Comanda PDF: accesible por roles operativos
                .antMatchers("/api/reportes/ticket/**").authenticated()
                .antMatchers("/api/reportes/comanda/**").authenticated()

                // Categorias & Productos: Mesero GET, Admin todo
                .antMatchers(HttpMethod.GET, "/api/categorias/**").hasAnyRole("ADMIN", "MESERO")
                .antMatchers(HttpMethod.POST, "/api/categorias/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/categorias/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/categorias/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/api/productos/**").hasAnyRole("ADMIN", "MESERO")
                .antMatchers(HttpMethod.POST, "/api/productos/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")

                // Mesas: Cajero GET, Admin CRUD
                .antMatchers(HttpMethod.GET, "/api/mesas/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/mesas/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/mesas/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/mesas/**").hasRole("ADMIN")

                // Comensales: Cajero GET, Mesero/Admin POST/DELETE
                .antMatchers(HttpMethod.GET, "/api/comensales/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/comensales/**").hasAnyRole("ADMIN", "MESERO")
                .antMatchers(HttpMethod.DELETE, "/api/comensales/**").hasAnyRole("ADMIN", "MESERO")

                // Pedidos: cerrar-mesa para todos autenticados; POST/PUT/DELETE Admin/Mesero
                .antMatchers(HttpMethod.GET, "/api/pedidos/**").authenticated()
                .antMatchers("/api/pedidos/cerrar-mesa/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/pedidos/**").hasAnyRole("ADMIN", "MESERO")
                .antMatchers(HttpMethod.PUT, "/api/pedidos/**").hasAnyRole("ADMIN", "MESERO")
                .antMatchers(HttpMethod.DELETE, "/api/pedidos/**").hasAnyRole("ADMIN", "MESERO")

                // Any other /api/** requires authentication
                .antMatchers("/api/**").authenticated()

                // SPA routes (non-API) are public — frontend handles auth
                .anyRequest().permitAll()
            .and()
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
