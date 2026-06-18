package com.listasmart.api.controller;

import com.listasmart.api.dto.LeaderboardEntry;
import com.listasmart.api.security.CurrentUser;
import com.listasmart.api.service.RankingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ranking")
public class RankingController {

    private final RankingService ranking;

    public RankingController(RankingService ranking) {
        this.ranking = ranking;
    }

    /** Ranking global completo, com o usuario autenticado marcado. */
    @GetMapping
    public List<LeaderboardEntry> ranking() {
        return ranking.leaderboard(CurrentUser.id());
    }
}
