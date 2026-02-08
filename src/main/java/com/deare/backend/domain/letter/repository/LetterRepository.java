package com.deare.backend.domain.letter.repository;

import com.deare.backend.domain.letter.entity.Letter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long>, LetterRepositoryCustom {

    Optional<Letter> findByIdAndUser_IdAndIsDeletedFalse(Long id, Long userId);

    Optional<Letter> findByIdAndUserIdAndIsDeletedFalse(Long id, Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Letter l
           set l.folder = null
         where l.user.id = :userId
           and l.folder.id = :folderId
           and l.isDeleted = false
    """)
    int clearFolder(Long userId, Long folderId);

    /**
     * 해당 유저의 모든 편지 조회
     */
    List<Letter> findAllByUser_Id(Long userId);
}