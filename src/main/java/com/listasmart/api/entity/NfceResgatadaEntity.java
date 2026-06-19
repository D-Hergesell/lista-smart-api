package com.listasmart.api.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Registro de NFC-e ja resgatada (1 linha por chave de acesso). Garante, via
 * UNIQUE na chave, que um mesmo cupom nao gere pontos mais de uma vez no sistema
 * inteiro (anti-exploit, escopo global). Persiste mesmo apos o soft-delete das
 * contribuicoes, de modo que reescanear a mesma nota nunca volta a pontuar.
 */
@Entity
@Table(name = "nfce_resgatada",
        uniqueConstraints = @UniqueConstraint(name = "uk_nfce_chave", columnNames = "chave"))
public class NfceResgatadaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Chave de acesso de 44 digitos (identificador fiscal unico). */
    @Column(nullable = false, unique = true, length = 44)
    private String chave;

    /** Usuario que resgatou primeiro (auditoria). */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "redeemed_at", nullable = false)
    private Instant redeemedAt = Instant.now();

    protected NfceResgatadaEntity() {}

    public NfceResgatadaEntity(String chave, Long userId) {
        this.chave = chave;
        this.userId = userId;
        this.redeemedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getChave() { return chave; }
    public Long getUserId() { return userId; }
    public Instant getRedeemedAt() { return redeemedAt; }
}
