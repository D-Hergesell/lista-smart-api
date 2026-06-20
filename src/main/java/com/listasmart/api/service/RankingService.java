package com.listasmart.api.service;

import com.listasmart.api.dto.LeaderboardEntry;
import com.listasmart.api.gamification.RankTable;
import com.listasmart.api.repository.ContributionRepository;
import com.listasmart.api.repository.projection.RankingRow;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Ranking global: soma de pontos ativos por usuario, decrescente. O usuario
 * autenticado e marcado com currentUser=true para o app destaca-lo.
 */
@Service
public class RankingService {

    private final ContributionRepository repo;

    public RankingService(ContributionRepository repo) {
        this.repo = repo;
    }

    /** Lista completa do ranking, ja ordenada e com o usuario atual marcado. */
    public List<LeaderboardEntry> leaderboard(Long currentUserId) {
        List<RankingRow> rows = repo.findRanking();
        List<LeaderboardEntry> list = new ArrayList<>(rows.size());
        for (RankingRow r : rows) {
            boolean isCurrent = currentUserId != null && currentUserId.equals(r.getUserId());
            String badge = RankTable.RANKS.get(RankTable.indexForPoints(r.getPoints())).name();
            list.add(new LeaderboardEntry(
                    r.getUsername(),
                    r.getPoints(),
                    r.getContributions(),
                    AvatarUtil.initials(r.getUsername()),
                    isCurrent,
                    badge));
        }
        return list;
    }

    /** Posicao 1-based do usuario no ranking (0 se nao encontrado). */
    public int positionOf(Long userId) {
        List<RankingRow> rows = repo.findRanking();
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).getUserId().equals(userId)) {
                return i + 1;
            }
        }
        return 0;
    }
}
