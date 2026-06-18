package com.listasmart.api.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extrai itens individuais do conteudo bruto de um QR Code de NFC-e.
 *
 * <p><b>Nota academica / limitacao real:</b> o QR Code oficial da NFC-e NAO
 * contem a lista de itens — ele carrega apenas a chave de acesso e parametros
 * para a pagina da SEFAZ (ex.: {@code ...?p=CHAVE|versao|tpAmb|...}). Obter os
 * itens exigiria consultar/raspar o portal estadual, o que esta fora do escopo
 * deste projeto.
 *
 * <p>Para manter a regra "1 contribuicao por item" coerente, o parser aceita um
 * formato textual simplificado, com um item por linha no padrao:
 * <pre>NOME_DO_ITEM ; PRECO</pre>
 * (separadores aceitos: ';' ou '|'; preco com ',' ou '.'). Quando o conteudo
 * for uma URL de NFC-e (ou nao casar com o padrao), faz fallback para UM unico
 * item representando o cupom inteiro.
 */
@Component
public class NfceQrParser {

    /** Item extraido do cupom. */
    public record Item(String name, BigDecimal price) {}

    // NOME <sep> PRECO   ex.: "Arroz 5kg ; 24,90"   "Café | 15.00"
    private static final Pattern LINE =
            Pattern.compile("^\\s*(.+?)\\s*[;|]\\s*R?\\$?\\s*(\\d+[.,]\\d{1,2})\\s*$");

    public List<Item> parse(String rawData) {
        List<Item> items = new ArrayList<>();
        if (rawData == null || rawData.isBlank()) {
            return items; // caller trata como invalido
        }

        for (String line : rawData.split("\\r?\\n")) {
            Matcher m = LINE.matcher(line);
            if (m.matches()) {
                String name = m.group(1).trim();
                BigDecimal price = new BigDecimal(m.group(2).replace(",", "."));
                items.add(new Item(name, price));
            }
        }

        // Fallback: nenhum item estruturado reconhecido (ex.: URL da SEFAZ).
        if (items.isEmpty()) {
            items.add(new Item("Cupom NFC-e", null));
        }
        return items;
    }
}
