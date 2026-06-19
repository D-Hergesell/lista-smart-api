package com.listasmart.api.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.listasmart.api.nfce.NfceKey;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Utilitario de DESENVOLVIMENTO para gerar QR Codes de NFC-e (SC) de teste —
 * reproduz o formato real da URL de consulta da SEFAZ-SC, com uma chave de
 * acesso valida (DV correto) porem ficticia. Serve para produzir os cupons da
 * simulacao/demo sem depender de ferramenta externa.
 *
 * <p>Desligado por padrao em producao: ativa apenas com
 * {@code app.dev-tools.enabled=true}.
 *
 * <pre>
 *  GET /dev/nfce/key         -> gera uma chave SC valida (JSON: chave + url)
 *  GET /dev/nfce/qr          -> PNG do QR com uma chave SC nova
 *  GET /dev/nfce/qr?chave=.. -> PNG do QR para uma chave especifica
 * </pre>
 */
@RestController
@RequestMapping("/dev/nfce")
@ConditionalOnProperty(name = "app.dev-tools.enabled", havingValue = "true")
public class DevNfceController {

    /** URL de consulta publica da NFC-e de Santa Catarina (formato de producao). */
    private static final String SC_CONSULTA = "https://sat.sef.sc.gov.br/nfce/consulta?p=";

    private static final int QR_SIZE = 320;

    private final Random rnd = new Random();

    @GetMapping("/key")
    public Map<String, String> key() {
        String chave = NfceKey.generateSc(rnd).chave();
        Map<String, String> body = new LinkedHashMap<>();
        body.put("chave", chave);
        body.put("url", buildUrl(chave));
        return body;
    }

    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qr(@RequestParam(value = "chave", required = false) String chave) throws Exception {
        NfceKey key = (chave == null || chave.isBlank())
                ? NfceKey.generateSc(rnd)
                : NfceKey.parse(chave); // valida a chave informada
        BitMatrix matrix = new QRCodeWriter()
                .encode(buildUrl(key.chave()), BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", png);
        return png.toByteArray();
    }

    /** p = chave | versaoQR | tpAmb | idToken | hash (idToken/hash ficticios no mock). */
    private static String buildUrl(String chave) {
        return SC_CONSULTA + chave + "|2|1|000001|0";
    }
}
