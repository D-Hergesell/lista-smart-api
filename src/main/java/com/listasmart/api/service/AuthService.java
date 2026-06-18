package com.listasmart.api.service;

import com.listasmart.api.dto.AuthDtos.AuthResponse;
import com.listasmart.api.dto.AuthDtos.Credentials;
import com.listasmart.api.entity.UserEntity;
import com.listasmart.api.exception.ApiException;
import com.listasmart.api.repository.UserRepository;
import com.listasmart.api.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Cadastro e login simplificados: username unico + senha (BCrypt) e token JWT.
 * Sem email, sem roles, sem recuperacao de senha.
 */
@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthResponse register(Credentials c) {
        String username = c.username().trim();
        if (username.isEmpty()) {
            throw ApiException.badRequest("username é obrigatório");
        }
        if (c.password() == null || c.password().length() < 6) {
            throw ApiException.badRequest("a senha deve ter ao menos 6 caracteres");
        }
        if (users.existsByUsername(username)) {
            throw ApiException.conflict("Este nome de usuário já está em uso");
        }

        UserEntity u = new UserEntity();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(c.password())); // nunca texto puro
        u = users.save(u);

        String token = jwt.generateToken(u.getId(), u.getUsername());
        return new AuthResponse(token, u.getId(), u.getUsername());
    }

    public AuthResponse login(Credentials c) {
        UserEntity u = users.findByUsername(c.username() == null ? "" : c.username().trim())
                .orElseThrow(() -> ApiException.unauthorized("Usuário ou senha inválidos"));

        if (!encoder.matches(c.password(), u.getPasswordHash())) {
            throw ApiException.unauthorized("Usuário ou senha inválidos");
        }

        String token = jwt.generateToken(u.getId(), u.getUsername());
        return new AuthResponse(token, u.getId(), u.getUsername());
    }
}
