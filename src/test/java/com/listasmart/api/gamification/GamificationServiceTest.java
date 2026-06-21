package com.listasmart.api.gamification;

import com.listasmart.api.dto.RankProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Testes do cálculo de selo (rank) e progresso a partir dos pontos acumulados.
 */
class GamificationServiceTest {

    private final GamificationService service = new GamificationService();

    @Test
    void pontuacaoZeroComecaNoFerroIv() {
        RankProgress p = service.progressFor(0);

        assertEquals("Ferro IV", p.currentRank());
        assertEquals("Ferro III", p.nextRank());
        assertEquals(0, p.currentThreshold());
        assertEquals(20, p.nextThreshold());
        assertEquals(0, p.progressPercent());
    }

    @Test
    void progressoNoMeioDaFaixaCalcula50PorCento() {
        // Ferro IV vai de 0 a 20; 10 pts = metade do caminho.
        RankProgress p = service.progressFor(10);

        assertEquals("Ferro IV", p.currentRank());
        assertEquals(50, p.progressPercent());
    }

    @Test
    void atingirOLimiarSobeDeSelo() {
        RankProgress p = service.progressFor(20);

        assertEquals("Ferro III", p.currentRank());
        assertEquals("Ferro II", p.nextRank());
    }

    @Test
    void rankMaximoNaoTemProximoEFica100PorCento() {
        RankProgress p = service.progressFor(6000); // acima de Desafiante (5500)

        assertEquals("Desafiante", p.currentRank());
        assertNull(p.nextRank());
        assertNull(p.nextThreshold());
        assertEquals(100, p.progressPercent());
    }
}
