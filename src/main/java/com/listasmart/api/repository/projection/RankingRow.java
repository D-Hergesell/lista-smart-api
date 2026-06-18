package com.listasmart.api.repository.projection;

/** Projecao de leitura do ranking (uma linha por usuario). */
public interface RankingRow {
    Long getUserId();
    String getUsername();
    int getPoints();
    int getContributions();
}
