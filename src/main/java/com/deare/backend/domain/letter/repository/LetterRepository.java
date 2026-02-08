package com.deare.backend.domain.letter.repository;

import com.deare.backend.domain.letter.entity.Letter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long>, LetterRepositoryCustom {
    Optional<Letter> findByIdAndUserIdAndIsDeletedFalse(Long id, Long userId);
}