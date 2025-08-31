package com.evaluation.project.repository;

import com.evaluation.project.model.entity.PhoneEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface PhoneRepository extends R2dbcRepository<PhoneEntity, Integer> {
  
  Flux<PhoneEntity> findByUuid(UUID uuid);
}
