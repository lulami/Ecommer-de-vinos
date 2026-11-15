package com.vinico.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tipo;
    private Long usuarioId;
    private String nombre;
    private String email;
    private String rol;
}
