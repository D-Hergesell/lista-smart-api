package com.listasmart.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTOs de autenticacao (login simplificado). */
public final class AuthDtos {

    private AuthDtos() {}

    /** POST /auth/register e POST /auth/login. */
    public record Credentials(
            @NotBlank(message = "username é obrigatório")
            @Size(min = 1, max = 50, message = "username inválido")
            String username,

            @NotBlank(message = "password é obrigatório")
            @Size(min = 6, message = "a senha deve ter ao menos 6 caracteres")
            String password
    ) {}

    /** Resposta de register/login: token + dados basicos para a sessao. */
    public record AuthResponse(String token, long userId, String username) {}
}
