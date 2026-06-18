package com.listasmart.api.controller;

import com.listasmart.api.dto.CatalogItem;
import com.listasmart.api.repository.MarketRepository;
import com.listasmart.api.repository.ProductRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Catalogo publico (sem autenticacao) para os Spinners do app. */
@RestController
public class CatalogController {

    private final ProductRepository products;
    private final MarketRepository markets;

    public CatalogController(ProductRepository products, MarketRepository markets) {
        this.products = products;
        this.markets = markets;
    }

    @GetMapping("/products")
    public List<CatalogItem> products() {
        return products.findAll().stream()
                .map(p -> new CatalogItem(String.valueOf(p.getId()), p.getName()))
                .toList();
    }

    @GetMapping("/markets")
    public List<CatalogItem> markets() {
        return markets.findAll().stream()
                .map(m -> new CatalogItem(String.valueOf(m.getId()), m.getName()))
                .toList();
    }
}
