package com.vinico.config;

import com.vinico.security.CustomUserDetailsService;
import com.vinico.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas — catálogo de vinos
                .requestMatchers(HttpMethod.GET, "/api/productos").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                // Auth pública
                .requestMatchers("/api/auth/**").permitAll()
                // Telemetría pública (IoT ingest sin auth para dispositivos)
                .requestMatchers(HttpMethod.POST, "/api/telemetry/ingest").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/telemetry/stream/**").permitAll()
                // H2 console (solo desarrollo)
                .requestMatchers("/h2-console/**").permitAll()
                // Actuator básico
                .requestMatchers("/actuator/health").permitAll()
                // Rutas admin — solo COMERCIANTE
                .requestMatchers("/api/admin/**").hasRole("COMERCIANTE")
                .requestMatchers(HttpMethod.POST, "/api/productos/**").hasRole("COMERCIANTE")
                .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("COMERCIANTE")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("COMERCIANTE")
                // Resto requiere autenticación
                .anyRequest().authenticated()
            )
            // Añadimos JwtFilter antes del filtro de usuario/contraseña
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            // Permite frames para H2 console
            .headers(headers ->
                headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://localhost:5174",
            "http://localhost:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
