package com.listasmart.api.service;

import com.listasmart.api.dto.ContributionRequest;
import com.listasmart.api.dto.ContributionResponse;
import com.listasmart.api.entity.ContributionEntity;
import com.listasmart.api.exception.ApiException;
import com.listasmart.api.repository.ContributionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD de contribuicoes com a regra de pontos:
 *  - QR: 10 pts por item extraido (1 linha por item).
 *  - Manual: 5 pts por envio (1 linha).
 *  - Edicao: nao altera pontos nem tipo.
 *  - Exclusao: soft delete (status='deleted'); os pontos saem da soma porque
 *    o total e SEMPRE derivado das contribuicoes 'active' (sem saldo
 *    denormalizado, portanto sem risco de divergencia).
 */
@Service
public class ContributionService {

    /** Constantes equivalentes ao GamificationHelper do app. */
    public static final int POINTS_QR = 10;
    public static final int POINTS_MANUAL = 5;

    private final ContributionRepository repo;
    private final NfceQrParser qrParser;

    public ContributionService(ContributionRepository repo, NfceQrParser qrParser) {
        this.repo = repo;
        this.qrParser = qrParser;
    }

    @Transactional
    public List<ContributionResponse> create(Long userId, ContributionRequest req) {
        String type = req.type() == null ? "" : req.type().trim().toLowerCase();
        return switch (type) {
            case ContributionEntity.TYPE_MANUAL -> List.of(createManual(userId, req));
            case ContributionEntity.TYPE_QR -> createFromQr(userId, req);
            default -> throw ApiException.badRequest("type inválido (use 'qr' ou 'manual')");
        };
    }

    private ContributionResponse createManual(Long userId, ContributionRequest req) {
        if (isBlank(req.product())) throw ApiException.badRequest("product é obrigatório");
        if (isBlank(req.market())) throw ApiException.badRequest("market é obrigatório");
        if (req.price() == null || req.price() <= 0) throw ApiException.badRequest("price deve ser maior que zero");

        ContributionEntity e = new ContributionEntity();
        e.setUserId(userId);
        e.setType(ContributionEntity.TYPE_MANUAL);
        e.setProduct(req.product().trim());
        e.setMarket(req.market().trim());
        e.setPrice(BigDecimal.valueOf(req.price()));
        e.setDate(parseDateNotFuture(req.date()));
        e.setSubmittedAt(Instant.now());
        e.setPoints(POINTS_MANUAL);
        return ContributionResponse.from(repo.save(e));
    }

    private List<ContributionResponse> createFromQr(Long userId, ContributionRequest req) {
        if (isBlank(req.rawData())) throw ApiException.badRequest("rawData é obrigatório para type=qr");

        List<NfceQrParser.Item> items = qrParser.parse(req.rawData());
        Instant now = Instant.now();
        List<ContributionResponse> out = new ArrayList<>();

        // Uma contribuicao por item extraido, cada uma valendo POINTS_QR.
        for (NfceQrParser.Item item : items) {
            ContributionEntity e = new ContributionEntity();
            e.setUserId(userId);
            e.setType(ContributionEntity.TYPE_QR);
            e.setProduct(item.name());
            e.setPrice(item.price());
            e.setRawData(req.rawData());
            e.setSubmittedAt(now);
            e.setPoints(POINTS_QR);
            out.add(ContributionResponse.from(repo.save(e)));
        }
        return out;
    }

    public List<ContributionResponse> listForUser(Long userId) {
        return repo.findByUserIdAndStatusOrderBySubmittedAtDesc(userId, ContributionEntity.STATUS_ACTIVE)
                .stream().map(ContributionResponse::from).toList();
    }

    @Transactional
    public ContributionResponse update(Long userId, Long id, ContributionRequest req) {
        ContributionEntity e = ownedActive(userId, id);

        // Edicao atualiza apenas os dados; NUNCA mexe em points nem em type.
        if (req.product() != null) e.setProduct(req.product().trim());
        if (req.market() != null) e.setMarket(req.market().trim());
        if (req.price() != null) {
            if (req.price() <= 0) throw ApiException.badRequest("price deve ser maior que zero");
            e.setPrice(BigDecimal.valueOf(req.price()));
        }
        if (req.date() != null) e.setDate(parseDateNotFuture(req.date()));
        return ContributionResponse.from(repo.save(e));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        ContributionEntity e = ownedActive(userId, id);
        e.setStatus(ContributionEntity.STATUS_DELETED); // estorno automatico via SUM(active)
        repo.save(e);
    }

    // ---- helpers ----------------------------------------------------------

    private ContributionEntity ownedActive(Long userId, Long id) {
        ContributionEntity e = repo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Contribuição não encontrada"));
        if (!e.getUserId().equals(userId)) {
            throw ApiException.forbidden("Você só pode alterar suas próprias contribuições");
        }
        if (ContributionEntity.STATUS_DELETED.equals(e.getStatus())) {
            throw ApiException.notFound("Contribuição não encontrada");
        }
        return e;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private LocalDate parseDateNotFuture(String iso) {
        if (isBlank(iso)) return null;
        try {
            LocalDate d = LocalDate.parse(iso.trim());
            if (d.isAfter(LocalDate.now())) {
                throw ApiException.badRequest("A data da compra não pode ser no futuro");
            }
            return d;
        } catch (DateTimeParseException ex) {
            throw ApiException.badRequest("date inválida (use o formato yyyy-MM-dd)");
        }
    }
}
