package com.deare.backend.domain.letter.repository;

import com.deare.backend.domain.letter.entity.LetterSearchToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LetterSearchTokenRepository
        extends JpaRepository<LetterSearchToken, Long>, LetterSearchTokenRepositoryCustom {

    boolean existsByLetter_IdAndIndexKeyVersion(Long letterId, int indexKeyVersion);

    @Modifying(flushAutomatically = true)
    @Query("delete from LetterSearchToken token where token.letter.id = :letterId")
    void deleteAllByLetterId(@Param("letterId") Long letterId);
}
