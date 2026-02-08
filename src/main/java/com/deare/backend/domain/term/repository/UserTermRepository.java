package com.deare.backend.domain.term.repository;

import com.deare.backend.domain.term.entity.UserTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTermRepository extends JpaRepository<UserTerm, Long> {

    /**
     * 사용자의 모든 약관 동의 내역 조회
     */
    List<UserTerm> findByUserId(Long userId);

    /**
     * 사용자의 특정 약관 동의 여부 확인
     */
    boolean existsByUserIdAndTermId(Long userId, Long termId);

    /**
     * 해당 유저의 모든 userTerm 삭제
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UserTerm ut WHERE ut.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
