package com.listasmart.api.dto;

/**
 * Item do ranking. Nomes espelham o model Android {@code LeaderboardUser}
 * (name, points, contributions, avatar, currentUser).
 */
public record LeaderboardEntry(
        String name,
        int points,
        int contributions,
        String avatar,
        boolean currentUser
) {}
