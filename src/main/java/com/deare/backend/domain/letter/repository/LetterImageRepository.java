package com.deare.backend.domain.letter.repository;

import com.deare.backend.domain.letter.entity.LetterImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LetterImageRepository extends JpaRepository<LetterImage, Long> {

    @Query("""
        select li.image.id
        from LetterImage li
        join li.letter l
        where l.user.id = :userId
          and li.image.id in :imageIds
    """)
    List<Long> findOwnedImageIds(Long userId, List<Long> imageIds);
}
