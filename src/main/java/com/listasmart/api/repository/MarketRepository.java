package com.listasmart.api.repository;

import com.listasmart.api.entity.MarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketRepository extends JpaRepository<MarketEntity, Long> {
}
