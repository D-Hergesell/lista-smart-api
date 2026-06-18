package com.listasmart.api.dto;

/**
 * Selo atual e progresso ate o proximo. {@code nextRank}/{@code nextThreshold}
 * vem nulos quando o usuario ja esta no rank maximo (Desafiante).
 */
public record RankProgress(
        String currentRank,
        String nextRank,
        int points,
        int currentThreshold,
        Integer nextThreshold,
        int progressPercent
) {}
