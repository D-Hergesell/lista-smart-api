package com.listasmart.api.service;

import com.listasmart.api.dto.ContributionRequest;
import com.listasmart.api.dto.ContributionResponse;
import com.listasmart.api.entity.ContributionEntity;
import com.listasmart.api.exception.ApiException;
import com.listasmart.api.nfce.NfceItemResolver;
import com.listasmart.api.nfce.NfceKey;
import com.listasmart.api.nfce.NfceNota;
import com.listasmart.api.repository.ContributionRepository;
import com.listasmart.api.repository.NfceResgatadaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Testes da regra de negócio central de contribuições:
 *  - pontuação (5 pts manual / 10 pts por item de QR);
 *  - anti-duplicidade global da NFC-e;
 *  - validações (tipo, preço, data).
 *
 * Usa Mockito (sem banco/Spring) para isolar o {@link ContributionService}.
 */
@ExtendWith(MockitoExtension.class)
class ContributionServiceTest {

    @Mock private ContributionRepository repo;
    @Mock private NfceItemResolver nfceResolver;
    @Mock private NfceResgatadaRepository resgatadaRepo;

    private ContributionService service;

    @BeforeEach
    void setUp() {
        service = new ContributionService(repo, nfceResolver, resgatadaRepo);
    }

    /** Faz o repo.save devolver a própria entidade com um id atribuído. */
    private void stubSaveWithId() {
        when(repo.save(any(ContributionEntity.class))).thenAnswer(inv -> {
            ContributionEntity e = inv.getArgument(0);
            if (e.getId() == null) e.setId(1L);
            return e;
        });
    }

    @Test
    void cadastroManualGera5Pontos() {
        stubSaveWithId();
        ContributionRequest req = new ContributionRequest(
                "manual", "Arroz", "Mercado X", 10.5,
                LocalDate.now().minusDays(1).toString(), null);

        List<ContributionResponse> out = service.create(1L, req);

        assertEquals(1, out.size());
        ContributionResponse r = out.get(0);
        assertEquals("manual", r.type());
        assertEquals(5, r.points());
        assertEquals("Arroz", r.product());
        assertEquals("Mercado X", r.market());
        assertEquals(10.5, r.price(), 0.001);
    }

    @Test
    void leituraDeQrGera10PontosPorItem() {
        String chave = NfceKey.generateSc(new Random(123)).chave();
        NfceNota nota = new NfceNota("Super SC", LocalDate.now().minusDays(2), List.of(
                new NfceNota.Item("Leite", new BigDecimal("4.99")),
                new NfceNota.Item("Pão", new BigDecimal("8.00"))));

        when(resgatadaRepo.existsByChave(anyString())).thenReturn(false);
        when(nfceResolver.resolve(any(NfceKey.class))).thenReturn(nota);
        stubSaveWithId();

        ContributionRequest req = new ContributionRequest("qr", null, null, null, null, chave);
        List<ContributionResponse> out = service.create(7L, req);

        assertEquals(2, out.size());                       // 1 contribuição por item
        assertEquals(10, out.get(0).points());
        assertEquals(10, out.get(1).points());
        assertEquals("qr", out.get(0).type());
        assertEquals("Leite", out.get(0).product());
        assertEquals("Super SC", out.get(0).market());
        assertEquals(4.99, out.get(0).price(), 0.001);
    }

    @Test
    void nfceJaResgatadaEhRejeitada() {
        String chave = NfceKey.generateSc(new Random(99)).chave();
        when(resgatadaRepo.existsByChave(anyString())).thenReturn(true);

        ContributionRequest req = new ContributionRequest("qr", null, null, null, null, chave);

        ApiException ex = assertThrows(ApiException.class, () -> service.create(1L, req));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void tipoInvalidoEhRejeitado() {
        ContributionRequest req = new ContributionRequest("xpto", "p", "m", 1.0, null, null);

        ApiException ex = assertThrows(ApiException.class, () -> service.create(1L, req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void precoZeroOuNegativoEhRejeitado() {
        ContributionRequest req = new ContributionRequest("manual", "Arroz", "Mercado", 0.0, null, null);

        ApiException ex = assertThrows(ApiException.class, () -> service.create(1L, req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void produtoEmBrancoEhRejeitado() {
        ContributionRequest req = new ContributionRequest("manual", "   ", "Mercado", 5.0, null, null);

        ApiException ex = assertThrows(ApiException.class, () -> service.create(1L, req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void dataFuturaEhRejeitada() {
        ContributionRequest req = new ContributionRequest(
                "manual", "Arroz", "Mercado", 5.0,
                LocalDate.now().plusDays(1).toString(), null);

        ApiException ex = assertThrows(ApiException.class, () -> service.create(1L, req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
