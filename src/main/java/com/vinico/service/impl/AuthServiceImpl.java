package com.vinico.service.impl;

import com.vinico.dto.request.LoginRequest;
import com.vinico.dto.request.RegistroRequest;
import com.vinico.dto.response.AuthResponse;
import com.vinico.exception.RecursoNoEncontradoException;
import com.vinico.model.Usuario;
import com.vinico.repository.UsuarioRepository;
import com.vinico.security.JwtUtil;
import com.vinico.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse registro(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado: " + request.getEmail());
        }

        Usuario.Rol rol;
        try {
            rol = (request.getRol() != null)
                    ? Usuario.Rol.valueOf(request.getRol().toUpperCase())
                    : Usuario.Rol.CLIENTE;
        } catch (IllegalArgumentException e) {
            rol = Usuario.Rol.CLIENTE;
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(rol)
                .activo(true)
                .build();

        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario, rol.name());
        return buildResponse(token, usuario);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BadCredentialsException("Usuario inactivo");
        }

        String token = jwtUtil.generateToken(usuario, usuario.getRol().name());
        return buildResponse(token, usuario);
    }

    private AuthResponse buildResponse(String token, Usuario usuario) {
        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .usuarioId(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .build();
    }
}
