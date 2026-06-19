package com.listasmart.api.repository;

import com.listasmart.api.entity.NfceResgatadaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NfceResgatadaRepository extends JpaRepository<NfceResgatadaEntity, Long> {

    /** Anti-duplicidade global: a chave ja foi resgatada por qualquer usuario? */
    boolean existsByChave(String chave);
}
