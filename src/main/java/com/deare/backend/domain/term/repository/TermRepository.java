package com.deare.backend.domain.term.repository;

import com.deare.backend.domain.term.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermRepository extends JpaRepository<Term, Long> {
    
    /**
     * 여러 약관 ID로 조회
     */
    List<Term> findByIdIn(List<Long> termIds);
}
