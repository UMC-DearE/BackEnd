package com.deare.backend.domain.setting.repository;

import com.deare.backend.domain.setting.entity.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSettingRepository extends JpaRepository<UserSetting,Long> {

    Optional<UserSetting> findByUser_Id(Long userId);
}
