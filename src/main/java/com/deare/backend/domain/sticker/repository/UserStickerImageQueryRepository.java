package com.deare.backend.domain.sticker.repository;

import com.deare.backend.domain.sticker.entity.UserSticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserStickerImageQueryRepository extends JpaRepository<UserSticker, Long> {

    @Query("""
        select us.image.id
        from UserSticker us
        where us.user.id = :userId
          and us.image.id in :imageIds
    """)
    List<Long> findOwnedImageIds(
            @Param("userId") Long userId,
            @Param("imageIds") List<Long> imageIds
    );
}
