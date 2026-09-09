package com.deare.backend.domain.sticker.repository;

import com.deare.backend.domain.sticker.entity.UserSticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserStickerRepository extends JpaRepository<UserSticker,Long> {

    @Query("""
        select us
          from UserSticker us
          join fetch us.image
         where us.user.id = :userId
         order by us.posZ asc
    """)
    List<UserSticker> findAllWithImageByUser_IdOrderByPosZAsc(@Param("userId") Long userId);

    /**
     * 해당 유저의 모든 userSticker 삭제
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UserSticker us WHERE us.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
