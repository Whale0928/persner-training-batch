package com.batch.pt.persnertrainingbatch.repository.packaze;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SpringBootTest
@ActiveProfiles("test") // application-test.yml
class PackageRepositoryTest {

    @Autowired
    private PackageRepository packageRepository;

    private static Package getBuild(String name, int count, int period) {
        return Package.builder()
                .packageName(name)
                .count(count)
                .period(period)
                .build();
    }

    @Test
    @DisplayName("데이터를 저장 후 Seq를 확인 할 수 있다.")
    void test_save() throws Exception {
        // given
        Package aPackage = getBuild("test", 1, 82);
        // when
        packageRepository.save(aPackage);

        // then
        assertNotNull(aPackage.getPackageSeq());
    }

    @Test
    @DisplayName("특정 시점의 데이터 하나를  조회할 수 있다.")
    void test_find() throws Exception {
        // given
        LocalDateTime time = LocalDateTime.now().minusMinutes(30);//30분전

        Package aPackage_3 = getBuild("3개월", 1, 90);
        Package aPackage_6 = getBuild("6개월", 1, 160);

        packageRepository.save(aPackage_3);
        packageRepository.save(aPackage_6);

        // when
        List<Package> list = packageRepository.findByCreatedAtAfter(time, PageRequest.of(0, 1, Sort.by("packageSeq").descending()));

        // then
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("package 정보를 수정할 수 있다.")
    void test_update() throws Exception {
        // given
        Package aPackage = getBuild("4개월 30회권", 30, 120);
        packageRepository.save(aPackage);

        // when
        // 40회, 150일로 수정
        int updateCount = packageRepository.updateCountAndPeriod(aPackage.getPackageSeq(), 40, 150);
        packageRepository.flush();
        Package updatePackage = packageRepository.findById(aPackage.getPackageSeq()).orElse(null);

        // then
        assertEquals(1, updateCount);
        assertEquals(40, updatePackage.getCount());
        assertEquals(150, updatePackage.getPeriod());
    }

    @Test
    @DisplayName("package 정보를 삭제할 수 있다.")
    void test_delete() throws Exception {
        // given
        Package aPackage = getBuild("4개월 30회권", 30, 120);
        packageRepository.save(aPackage);

        // when
        packageRepository.delete(aPackage);
        Package deletePackage = packageRepository.findById(aPackage.getPackageSeq()).orElse(null);

        // then
        assertEquals(null, deletePackage);
    }
}