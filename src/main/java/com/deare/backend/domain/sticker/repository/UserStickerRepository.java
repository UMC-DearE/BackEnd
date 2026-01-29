package com.deare.backend.domain.sticker.repository;

import com.deare.backend.domain.sticker.entity.UserSticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStickerRepository extends JpaRepository<UserSticker,Long> {
    List<UserSticker>findAllByUser_IdOrderByPosZAsc(Long userId);
}
