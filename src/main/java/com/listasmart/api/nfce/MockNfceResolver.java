package com.listasmart.api.nfce;

import com.listasmart.api.repository.MarketRepository;
import com.listasmart.api.repository.ProductRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Implementacao mockada do {@link NfceItemResolver}. Gera uma nota plausivel e
 * DETERMINISTICA a partir da chave: a mesma chave produz sempre a mesma nota
 * (mesmo mercado, itens e precos), e chaves diferentes produzem notas
 * diferentes. Reaproveita o catalogo de produtos/mercados ja existente para que
 * os dados fiquem coerentes com o app.
 *
 * <p>Ativa por padrao ({@code app.nfce.provider=mock}). Para plugar uma fonte
 * real, basta criar outro {@link NfceItemResolver} com {@code havingValue}
 * diferente — esta classe sai e a outra entra, sem mexer no service.
 */
@Component
@ConditionalOnProperty(name = "app.nfce.provider", havingValue = "mock", matchIfMissing = true)
public class MockNfceResolver implements NfceItemResolver {

    /** Usados apenas se o catalogo do banco estiver vazio. */
    private static final List<String> FALLBACK_PRODUCTS = List.of(
            "Arroz", "Feijao", "Acucar", "Cafe", "Oleo", "Leite", "Pao", "Carne", "Frango", "Ovos");
    private static final List<String> FALLBACK_MARKETS = List.of(
            "Carrefour", "Pao de Acucar", "Extra", "Dia Supermercado", "Atacadao", "Assai");

    private final ProductRepository products;
    private final MarketRepository markets;

    public MockNfceResolver(ProductRepository products, MarketRepository markets) {
        this.products = products;
        this.markets = markets;
    }

    @Override
    public NfceNota resolve(NfceKey key) {
        List<String> productNames = orFallback(
                products.findAll().stream().map(p -> p.getName()).toList(), FALLBACK_PRODUCTS);
        List<String> marketNames = orFallback(
                markets.findAll().stream().map(m -> m.getName()).toList(), FALLBACK_MARKETS);

        // O mercado deriva do EMITENTE (CNPJ): mesmo emitente -> mesmo mercado.
        String market = pick(marketNames, key.cnpjEmitente().hashCode());

        // O restante da nota deriva da chave inteira (seed estavel).
        Random rnd = new Random(key.chave().hashCode());

        int count = Math.min(3 + rnd.nextInt(6), productNames.size()); // 3..8 itens
        List<String> pool = new ArrayList<>(productNames);
        List<NfceNota.Item> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String name = pool.remove(rnd.nextInt(pool.size())); // sem repeticao
            BigDecimal price = BigDecimal.valueOf(199 + rnd.nextInt(4801)) // R$1,99..R$49,99
                    .movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
            items.add(new NfceNota.Item(name, price));
        }
        return new NfceNota(market, deriveDate(key, rnd), items);
    }

    private static List<String> orFallback(List<String> fromDb, List<String> fallback) {
        return fromDb.isEmpty() ? fallback : fromDb;
    }

    private static String pick(List<String> list, int seed) {
        return list.get(Math.floorMod(seed, list.size()));
    }

    /** Data de emissao derivada do AAMM da chave; nunca no futuro. */
    private static LocalDate deriveDate(NfceKey key, Random rnd) {
        LocalDate today = LocalDate.now();
        int day = 1 + rnd.nextInt(28);
        LocalDate d;
        try {
            d = LocalDate.of(key.year(), key.month(), day);
        } catch (RuntimeException ex) {
            d = today; // AAMM invalido (chave fora do padrao): usa hoje
        }
        return d.isAfter(today) ? today : d;
    }
}
