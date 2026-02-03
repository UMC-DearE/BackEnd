package com.deare.backend.domain.term.repository;

import com.deare.backend.domain.term.entity.UserTerm;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
