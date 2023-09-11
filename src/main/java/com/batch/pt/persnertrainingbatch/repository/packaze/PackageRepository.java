package com.batch.pt.persnertrainingbatch.repository.packaze;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;


public interface PackageRepository extends JpaRepository<Package, Integer> {
    List<Package> findByCreatedAtAfter(LocalDateTime time, PageRequest pageRequest);

    @Modifying // 수정일 기록
    @Transactional
    @Query(value = "update package p" +
            "     set p.count = :count" +
            "      , p.period = :period " +
            "where p.package_seq = :packageSeq"
            , nativeQuery = true
    )
    int updateCountAndPeriod(Integer packageSeq, Integer count, Integer period);
}
