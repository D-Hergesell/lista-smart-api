package com.listasmart.api.nfce;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Nota resolvida a partir de uma NFC-e: o emitente (mercado), a data de emissao
 * e a lista de itens. E o formato comum devolvido por qualquer
 * {@link NfceItemResolver} (mock hoje, API real no futuro).
 */
public record NfceNota(String market, LocalDate date, List<Item> items) {

    /** Item da nota: produto e preco unitario pago. */
    public record Item(String product, BigDecimal price) {}
}
