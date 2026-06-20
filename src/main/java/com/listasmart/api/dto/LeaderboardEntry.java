package com.listasmart.api.dto;

/**
 * Item do ranking. Nomes espelham o model Android {@code LeaderboardUser}
 * (name, points, contributions, avatar, currentUser, badge).
 * {@code badge} é o Selo de Confiabilidade derivado dos pontos (RankTable).
 */
public record LeaderboardEntry(
        String name,
        int points,
        int contributions,
        String avatar,
        boolean currentUser,
        String badge
) {}
