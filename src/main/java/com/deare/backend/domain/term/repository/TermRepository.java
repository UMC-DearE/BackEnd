package com.deare.backend.domain.term.repository;

import com.deare.backend.domain.term.entity.Term;
import com.deare.backend.domain.term.entity.enums.TermType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermRepository extends JpaRepository<Term, Long> {

    /**
     * 여러 약관 ID로 조회
     */
    List<Term> findByIdIn(List<Long> termIds);

    /**
     * 활성화된 약관 목록 조회 (회원가입용)
     */
    List<Term> findByIsActiveTrue();

    /**
     * 활성화된 약관 목록 조회 - 타입 필터
     */
    List<Term> findByIsActiveTrueAndType(TermType type);
}
