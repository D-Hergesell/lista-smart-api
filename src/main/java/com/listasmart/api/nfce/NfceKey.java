package com.listasmart.api.nfce;

import com.listasmart.api.exception.ApiException;

import java.time.LocalDate;
import java.util.Random;

/**
 * Chave de acesso da NFC-e (44 digitos). Faz o parse a partir do conteudo bruto
 * do QR Code (URL da SEFAZ ou a propria chave), valida o digito verificador
 * (modulo 11) e expoe os campos estruturais (UF, CNPJ do emitente, ano/mes).
 *
 * <p>Estrutura dos 44 digitos:
 * <pre>
 * cUF(2) AAMM(4) CNPJ(14) mod(2) serie(3) nNF(9) tpEmis(1) cNF(8) cDV(1)
 * </pre>
 */
public record NfceKey(String chave) {

    /** Codigo de UF de Santa Catarina na tabela do IBGE/SEFAZ. */
    public static final String UF_SC = "42";

    public NfceKey {
        if (chave == null || !chave.matches("\\d{44}")) {
            throw ApiException.badRequest("Chave de acesso invalida (esperados 44 digitos)");
        }
        if (!checkDigitValid(chave)) {
            throw ApiException.badRequest("Chave de acesso invalida (digito verificador)");
        }
    }

    /** UF (2 primeiros digitos), ex.: "42" = SC. */
    public String uf() { return chave.substring(0, 2); }

    /** CNPJ do emitente (digitos 7-20). */
    public String cnpjEmitente() { return chave.substring(6, 20); }

    /** Ano de emissao (4 digitos), derivado do AAMM. */
    public int year() { return 2000 + Integer.parseInt(chave.substring(2, 4)); }

    /** Mes de emissao (1-12). */
    public int month() { return Integer.parseInt(chave.substring(4, 6)); }

    public boolean isSantaCatarina() { return UF_SC.equals(uf()); }

    // ---- parse a partir do conteudo do QR -------------------------------

    /**
     * Extrai e valida a chave a partir do conteudo bruto do QR Code. Aceita
     * tanto a URL da SEFAZ ({@code ...?p=CHAVE|...}) quanto a chave pura.
     */
    public static NfceKey parse(String rawData) {
        if (rawData == null || rawData.isBlank()) {
            throw ApiException.badRequest("Conteudo do QR Code vazio");
        }
        String s = rawData.trim();
        int p = s.indexOf("p=");
        if (p >= 0) {
            s = s.substring(p + 2);
        }
        // A chave e o primeiro campo (antes de '|' ou '&'); mantem so digitos.
        s = s.split("[|&]", 2)[0].replaceAll("\\D", "");
        return new NfceKey(s);
    }

    // ---- validacao do digito verificador (modulo 11) --------------------

    private static boolean checkDigitValid(String key) {
        return computeCheckDigit(key.substring(0, 43)) == key.charAt(43);
    }

    // ---- geracao de chave de teste (apenas para o mock/dev) -------------

    /**
     * Gera uma chave de SC valida (DV correto) com dados ficticios, para uso em
     * testes/demonstracao. Nao tem validade fiscal na SEFAZ.
     */
    public static NfceKey generateSc(Random rnd) {
        LocalDate now = LocalDate.now();
        StringBuilder b = new StringBuilder(44);
        b.append(UF_SC);                                              // cUF
        b.append(String.format("%02d%02d", now.getYear() % 100, now.getMonthValue())); // AAMM
        appendDigits(b, rnd, 14);                                    // CNPJ emitente
        b.append("65");                                              // modelo NFC-e
        appendDigits(b, rnd, 3);                                     // serie
        appendDigits(b, rnd, 9);                                     // nNF
        b.append('1');                                               // tpEmis (normal)
        appendDigits(b, rnd, 8);                                     // cNF
        b.append(computeCheckDigit(b.toString()));                   // cDV
        return new NfceKey(b.toString());
    }

    private static void appendDigits(StringBuilder b, Random rnd, int n) {
        for (int i = 0; i < n; i++) b.append(rnd.nextInt(10));
    }

    /** Digito verificador (modulo 11) dos 43 primeiros digitos. */
    private static char computeCheckDigit(String first43) {
        int sum = 0, weight = 2;
        for (int i = 42; i >= 0; i--) {
            sum += (first43.charAt(i) - '0') * weight;
            weight = (weight == 9) ? 2 : weight + 1;
        }
        int mod = sum % 11;
        int dv = (mod == 0 || mod == 1) ? 0 : 11 - mod;
        return (char) ('0' + dv);
    }
}
