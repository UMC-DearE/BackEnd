package com.deare.backend.domain.letter.repository;

import com.deare.backend.domain.letter.entity.LetterSearchToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LetterSearchTokenRepository
        extends JpaRepository<LetterSearchToken, Long>, LetterSearchTokenRepositoryCustom {
}
