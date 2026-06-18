package com.listasmart.api.controller;

import com.listasmart.api.dto.ContributionRequest;
import com.listasmart.api.dto.ContributionResponse;
import com.listasmart.api.exception.ApiException;
import com.listasmart.api.security.CurrentUser;
import com.listasmart.api.service.ContributionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contributions")
public class ContributionController {

    private final ContributionService service;

    public ContributionController(ContributionService service) {
        this.service = service;
    }

    /**
     * Cria contribuicao. Para type=qr pode retornar VARIAS contribuicoes
     * (uma por item extraido do cupom).
     */
    @PostMapping
    public List<ContributionResponse> create(@Valid @RequestBody ContributionRequest body) {
        return service.create(CurrentUser.id(), body);
    }

    /** Historico do usuario. So o proprio dono pode consultar. */
    @GetMapping("/user/{id}")
    public List<ContributionResponse> listForUser(@PathVariable("id") Long id) {
        requireSelf(id);
        return service.listForUser(id);
    }

    /** Edicao (nao gera pontos). So o dono. */
    @PutMapping("/{id}")
    public ContributionResponse update(@PathVariable("id") Long id,
                                       @RequestBody ContributionRequest body) {
        return service.update(CurrentUser.id(), id, body);
    }

    /** Exclusao (estorna pontos via soft delete). So o dono. */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable("id") Long id) {
        service.delete(CurrentUser.id(), id);
        return Map.of("deleted", true, "id", id);
    }

    private void requireSelf(Long id) {
        if (!id.equals(CurrentUser.id())) {
            throw ApiException.forbidden("Você só pode acessar suas próprias contribuições");
        }
    }
}
