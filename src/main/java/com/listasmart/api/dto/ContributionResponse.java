package com.listasmart.api.dto;

import com.listasmart.api.entity.ContributionEntity;

/**
 * Resposta de contribuicao. Os nomes de campo espelham EXATAMENTE o model
 * Android {@code Contribution} (Gson) — inclusive {@code submittedAt} como
 * epoch em milissegundos e {@code date} como "yyyy-MM-dd".
 */
public record ContributionResponse(
        long id,
        String type,
        String product,
        String market,
        double price,
        String date,
        String rawData,
        long submittedAt,
        int points,
        String status
) {
    public static ContributionResponse from(ContributionEntity e) {
        return new ContributionResponse(
                e.getId(),
                e.getType(),
                e.getProduct(),
                e.getMarket(),
                e.getPrice() == null ? 0.0 : e.getPrice().doubleValue(),
                e.getDate() == null ? null : e.getDate().toString(),
                e.getRawData(),
                e.getSubmittedAt() == null ? 0L : e.getSubmittedAt().toEpochMilli(),
                e.getPoints(),
                e.getStatus()
        );
    }
}
