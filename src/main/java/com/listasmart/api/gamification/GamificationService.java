package com.listasmart.api.gamification;

import com.listasmart.api.dto.RankProgress;
import org.springframework.stereotype.Service;

/**
 * Calcula o selo atual e o progresso ate o proximo a partir dos pontos
 * acumulados. Equivale (em conceito) ao antigo GamificationHelper do app,
 * mas agora baseado em PONTOS e nao em contagem de contribuicoes.
 */
@Service
public class GamificationService {

    public RankProgress progressFor(int points) {
        int idx = RankTable.indexForPoints(points);
        RankTable.Rank current = RankTable.RANKS.get(idx);

        boolean isMax = idx == RankTable.RANKS.size() - 1;
        if (isMax) {
            return new RankProgress(current.name(), null, points, current.minPoints(),
                    null, 100);
        }

        RankTable.Rank next = RankTable.RANKS.get(idx + 1);
        int floor = current.minPoints();
        int ceil = next.minPoints();
        int gained = points - floor;
        int span = ceil - floor;
        int percent = span <= 0 ? 100 : Math.min(100, (int) ((gained / (float) span) * 100));

        return new RankProgress(current.name(), next.name(), points, floor, ceil, percent);
    }
}
