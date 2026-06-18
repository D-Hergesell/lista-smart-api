package com.listasmart.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Contribuicao (leitura de QR ou cadastro manual). Uma linha por item.
 * Os nomes de campo espelham o model Android {@code Contribution} para
 * minimizar refatoracao no cliente.
 */
@Entity
@Table(name = "contributions")
public class ContributionEntity {

    public static final String TYPE_QR = "qr";
    public static final String TYPE_MANUAL = "manual";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_DELETED = "deleted";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 10)
    private String type;

    @Column(length = 120)
    private String product;

    @Column(length = 120)
    private String market;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    /** Coluna purchase_date (evita a palavra reservada DATE); JSON expoe "date". */
    @Column(name = "purchase_date")
    private LocalDate date;

    @Column(name = "raw_data", columnDefinition = "text")
    private String rawData;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt = Instant.now();

    @Column(nullable = false)
    private int points;

    @Column(nullable = false, length = 10)
    private String status = STATUS_ACTIVE;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
