package com.deare.backend.domain.setting.repository;

import com.deare.backend.domain.setting.entity.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserSettingRepository extends JpaRepository<UserSetting,Long> {

    Optional<UserSetting> findByUser_Id(Long userId);

    /**
     * 해당 유저의 userSetting 삭제
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UserSetting us WHERE us.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
