package com.deare.backend.domain.letter.repository;

import com.deare.backend.domain.letter.entity.Letter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long>, LetterRepositoryCustom {

    Optional<Letter> findByIdAndUser_IdAndIsDeletedFalse(Long id, Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Letter l
           set l.folder = null
         where l.user.id = :userId
           and l.folder.id = :folderId
           and l.isDeleted = false
    """)
    int clearFolder(@Param("userId") Long userId, @Param("folderId") Long folderId);

    List<Letter> findAllByUser_Id(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Letter l
       set l.isDeleted = true,
           l.deletedAt = CURRENT_TIMESTAMP
     where l.user.id = :userId
       and l.from.id = :fromId
       and l.isDeleted = false
""")
    int softDeleteAllByUserIdAndFromId(@Param("userId") Long userId,
                                       @Param("fromId") Long fromId);


    List<Letter> findAllByUser_IdAndFrom_IdAndIsDeletedFalse(Long userId, Long fromId);

    @Query("""
        select l.isPinned
            from Letter l
        where l.id = :letterId
            and l.user.id = :userId
            and l.isDeleted = false
            and l.isHidden = false
    """)
    Optional<Boolean> findIsPinnedByUserIdAndLetterId(
            @Param("userId") Long userId,
            @Param("letterId") Long letterId
    );

    @Query("""
        select l
            from Letter l
        where l.user.id = :userId
            and l.isPinned = true
            and l.isDeleted = false
            and l.isHidden = false
        order by l.updatedAt desc, l.id desc
    """)
    Optional<Letter> findTopPinnedByUserId(@Param("userId") Long userId);

}
