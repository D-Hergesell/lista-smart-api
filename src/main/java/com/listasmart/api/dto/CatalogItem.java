package com.listasmart.api.dto;

/**
 * Item de catalogo (product/market). {@code id} sai como String para casar
 * com os models Android {@code Product}/{@code Market} (que usam String id).
 */
public record CatalogItem(String id, String name) {}
