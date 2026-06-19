package com.listasmart.api.nfce;

/**
 * Resolve o conteudo (mercado + itens) de uma NFC-e a partir da chave de acesso.
 *
 * <p>Abstracao "plug-and-play": hoje existe a implementacao mockada
 * ({@link MockNfceResolver}); uma implementacao real (consulta a SEFAZ ou a uma
 * API paga) pode ser plugada sem tocar no resto do codigo, selecionada pela
 * property {@code app.nfce.provider}.
 */
public interface NfceItemResolver {

    /** Devolve a nota correspondente a chave. Nunca retorna {@code null}. */
    NfceNota resolve(NfceKey key);
}
