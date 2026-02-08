package com.deare.backend.domain.report.repository;

import com.deare.backend.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * 해당 유저의 모든 Report 삭제
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Report r WHERE r.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
