package com.listasmart.api.controller;

import com.listasmart.api.dto.RankProgress;
import com.listasmart.api.dto.UserMeResponse;
import com.listasmart.api.entity.ContributionEntity;
import com.listasmart.api.entity.UserEntity;
import com.listasmart.api.exception.ApiException;
import com.listasmart.api.gamification.GamificationService;
import com.listasmart.api.repository.ContributionRepository;
import com.listasmart.api.repository.UserRepository;
import com.listasmart.api.security.CurrentUser;
import com.listasmart.api.service.RankingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository users;
    private final ContributionRepository contributions;
    private final RankingService ranking;
    private final GamificationService gamification;

    public UserController(UserRepository users, ContributionRepository contributions,
                          RankingService ranking, GamificationService gamification) {
        this.users = users;
        this.contributions = contributions;
        this.ranking = ranking;
        this.gamification = gamification;
    }

    /** Perfil do usuario autenticado: pontos, contribuicoes, posicao e selo. */
    @GetMapping("/me")
    public UserMeResponse me() {
        Long userId = CurrentUser.id();
        UserEntity u = users.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Sessão inválida"));

        int points = contributions.sumActivePoints(userId);
        int count = contributions.countByUserIdAndStatus(userId, ContributionEntity.STATUS_ACTIVE);
        int position = ranking.positionOf(userId);
        RankProgress badge = gamification.progressFor(points);

        return new UserMeResponse(u.getId(), u.getUsername(), points, count, position, badge);
    }
}
