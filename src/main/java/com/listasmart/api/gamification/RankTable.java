package com.listasmart.api.gamification;

import java.util.List;

/**
 * Selos de Confiabilidade baseados em PONTOS ACUMULADOS (substitui as faixas
 * antigas por contagem de contribuicoes do GamificationHelper).
 *
 * <p>Calibragem: a curva e levemente progressiva (cada faixa exige um pouco
 * mais que a anterior). Referencia de ritmo:
 * <ul>
 *   <li>1 contribuicao manual = 5 pts; 1 item de QR = 10 pts.</li>
 *   <li>Usuario casual (~1-2 envios/dia => ~10-15 pts/dia) atinge as faixas
 *       Prata/Ouro em algumas semanas.</li>
 *   <li>As faixas finais (Mestre+) exigem milhares de pontos, levando meses
 *       de uso consistente, preservando o sentido de progressao.</li>
 * </ul>
 */
public final class RankTable {

    /** Um degrau do ranking: nome do selo e pontos minimos para alcanca-lo. */
    public record Rank(String name, int minPoints) {}

    /** Ordem crescente por minPoints. NUNCA reordenar sem reavaliar a curva. */
    public static final List<Rank> RANKS = List.of(
            new Rank("Ferro IV",       0),
            new Rank("Ferro III",      20),
            new Rank("Ferro II",       45),
            new Rank("Ferro I",        75),
            new Rank("Bronze IV",      110),
            new Rank("Bronze III",     150),
            new Rank("Bronze II",      195),
            new Rank("Bronze I",       245),
            new Rank("Prata IV",       300),
            new Rank("Prata III",      365),
            new Rank("Prata II",       435),
            new Rank("Prata I",        510),
            new Rank("Ouro IV",        600),
            new Rank("Ouro III",       700),
            new Rank("Ouro II",        810),
            new Rank("Ouro I",         930),
            new Rank("Platina IV",     1070),
            new Rank("Platina III",    1220),
            new Rank("Platina II",     1380),
            new Rank("Platina I",      1550),
            new Rank("Esmeralda IV",   1750),
            new Rank("Esmeralda III",  1970),
            new Rank("Esmeralda II",   2210),
            new Rank("Esmeralda I",    2470),
            new Rank("Diamante IV",    2760),
            new Rank("Diamante III",   3070),
            new Rank("Diamante II",    3400),
            new Rank("Diamante I",     3750),
            new Rank("Mestre",         4200),
            new Rank("Grão-Mestre",    4800),
            new Rank("Desafiante",     5500)
    );

    private RankTable() {}

    /** Indice do rank atual para uma pontuacao. */
    public static int indexForPoints(int points) {
        int idx = 0;
        for (int i = 0; i < RANKS.size(); i++) {
            if (points >= RANKS.get(i).minPoints()) {
                idx = i;
            } else {
                break;
            }
        }
        return idx;
    }
}
