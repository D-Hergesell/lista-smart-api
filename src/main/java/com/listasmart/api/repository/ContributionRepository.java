package com.listasmart.api.repository;

import com.listasmart.api.entity.ContributionEntity;
import com.listasmart.api.repository.projection.RankingRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContributionRepository extends JpaRepository<ContributionEntity, Long> {

    /** Historico do usuario: somente ativas, mais recentes primeiro. */
    List<ContributionEntity> findByUserIdAndStatusOrderBySubmittedAtDesc(Long userId, String status);

    @Query("SELECT COALESCE(SUM(c.points), 0) FROM ContributionEntity c " +
           "WHERE c.userId = :userId AND c.status = 'active'")
    int sumActivePoints(@Param("userId") Long userId);

    int countByUserIdAndStatus(Long userId, String status);

    /**
     * Ranking global: soma de pontos ativos por usuario, decrescente.
     * Inclui usuarios sem contribuicoes (LEFT JOIN) para aparecerem zerados.
     */
    @Query("SELECT u.id AS userId, u.username AS username, " +
           "       COALESCE(SUM(CASE WHEN c.status = 'active' THEN c.points ELSE 0 END), 0) AS points, " +
           "       COALESCE(SUM(CASE WHEN c.status = 'active' THEN 1 ELSE 0 END), 0) AS contributions " +
           "FROM UserEntity u LEFT JOIN ContributionEntity c ON c.userId = u.id " +
           "GROUP BY u.id, u.username " +
           "ORDER BY points DESC, u.username ASC")
    List<RankingRow> findRanking();
}
