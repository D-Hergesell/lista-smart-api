package com.listasmart.api.config;

import com.listasmart.api.entity.MarketEntity;
import com.listasmart.api.entity.ProductEntity;
import com.listasmart.api.repository.MarketRepository;
import com.listasmart.api.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Popula o catalogo na primeira subida, se estiver vazio. Usa os mesmos itens
 * do fallback local atual do app (MainActivity.seedDefaultCatalog). Roda apos o
 * Hibernate criar o schema, evitando problemas de ordem com data.sql.
 */
@Configuration
public class CatalogSeeder {

    @Bean
    CommandLineRunner seedCatalog(ProductRepository products, MarketRepository markets) {
        return args -> {
            if (products.count() == 0) {
                for (String name : List.of("Arroz", "Feijão", "Açúcar", "Café", "Óleo",
                        "Leite", "Pão", "Carne", "Frango", "Ovos")) {
                    ProductEntity p = new ProductEntity();
                    p.setName(name);
                    products.save(p);
                }
            }
            if (markets.count() == 0) {
                for (String name : List.of("Carrefour", "Pão de Açúcar", "Extra",
                        "Dia Supermercado", "Atacadão", "Assaí")) {
                    MarketEntity m = new MarketEntity();
                    m.setName(name);
                    markets.save(m);
                }
            }
        };
    }
}
