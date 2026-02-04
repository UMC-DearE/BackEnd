package com.deare.backend.domain.letter.repository;

import com.deare.backend.domain.letter.entity.LetterImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LetterImageRepository extends JpaRepository<LetterImage, Long> {

    @Query("""
        select li.image.id
        from LetterImage li
        join li.letter l
        where l.user.id = :userId
          and li.image.id in :imageIds
    """)
    List<Long> findOwnedImageIds(@Param("userId") Long userId, @Param("imageIds") List<Long> imageIds);

    @Query("""
        select li.image.id
        from LetterImage li
        where li.image.id in :imageIds
    """)
    List<Long> findLinkedImageIds(@Param("imageIds") List<Long> imageIds);
}
