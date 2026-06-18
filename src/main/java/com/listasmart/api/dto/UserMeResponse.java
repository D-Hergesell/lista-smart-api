package com.listasmart.api.dto;

/**
 * GET /users/me — perfil + gamificacao + posicao no ranking.
 */
public record UserMeResponse(
        long id,
        String name,          // = username (compativel com session do app)
        int points,
        int contributions,
        int rankingPosition,  // posicao 1-based no ranking global
        RankProgress badge    // selo atual + progresso
) {}
