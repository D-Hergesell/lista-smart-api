package com.listasmart.api.nfce;

import com.listasmart.api.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes da chave de acesso da NFC-e: estrutura (44 dígitos), dígito verificador
 * (módulo 11) e extração a partir do conteúdo bruto do QR Code.
 */
class NfceKeyTest {

    @Test
    void generateScProduzChaveValidaDeSantaCatarina() {
        NfceKey key = NfceKey.generateSc(new Random(1));

        assertEquals(44, key.chave().length());
        assertEquals("42", key.uf());          // 42 = Santa Catarina
        assertTrue(key.isSantaCatarina());
    }

    @Test
    void parseAceitaAChavePura() {
        String chave = NfceKey.generateSc(new Random(2)).chave();

        assertEquals(chave, NfceKey.parse(chave).chave());
    }

    @Test
    void parseExtraiAChaveDaUrlDaSefaz() {
        String chave = NfceKey.generateSc(new Random(3)).chave();
        String url = "http://nfce.sef.sc.gov.br/consulta?p=" + chave + "|2|1|1|abcdef";

        assertEquals(chave, NfceKey.parse(url).chave());
    }

    @Test
    void parseRejeitaTamanhoInvalido() {
        ApiException ex = assertThrows(ApiException.class, () -> NfceKey.parse("123"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void parseRejeitaConteudoVazio() {
        assertThrows(ApiException.class, () -> NfceKey.parse(""));
    }

    @Test
    void parseRejeitaDigitoVerificadorErrado() {
        String valida = NfceKey.generateSc(new Random(4)).chave();
        char ultimo = valida.charAt(43);
        char errado = (ultimo == '0') ? '1' : '0';   // garante um DV diferente do correto
        String adulterada = valida.substring(0, 43) + errado;

        ApiException ex = assertThrows(ApiException.class, () -> NfceKey.parse(adulterada));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
