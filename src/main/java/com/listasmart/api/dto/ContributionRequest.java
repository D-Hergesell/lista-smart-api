package com.listasmart.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Corpo de POST /contributions e PUT /contributions/{id}.
 *
 * <p>type = "manual": product + market + price + date.
 * <br>type = "qr": rawData (conteudo bruto do QR Code NFC-e).
 * <p>As validacoes especificas por tipo sao feitas no service.
 */
public record ContributionRequest(
        @NotBlank(message = "type é obrigatório (qr|manual)")
        String type,
        String product,
        String market,
        Double price,
        String date,      // "yyyy-MM-dd"
        String rawData
) {}
