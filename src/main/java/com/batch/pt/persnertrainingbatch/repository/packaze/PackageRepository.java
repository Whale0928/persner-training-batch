package com.batch.pt.persnertrainingbatch.repository.packaze;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface PackageRepository extends JpaRepository<Package, Integer> {
    List<Package> findByCreatedAtAfter(LocalDateTime time, PageRequest pageRequest);
}
