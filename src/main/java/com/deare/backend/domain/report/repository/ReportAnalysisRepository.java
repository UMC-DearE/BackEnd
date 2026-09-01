package com.deare.backend.domain.report.repository;

import com.deare.backend.domain.report.entity.ReportAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ReportAnalysisRepository extends JpaRepository<ReportAnalysis, Long> {
    Optional<ReportAnalysis> findByUserId(Long userId);


    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ReportAnalysis r WHERE r.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
