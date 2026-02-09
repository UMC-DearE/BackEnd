package com.deare.backend.domain.from.repository;

import com.deare.backend.domain.from.entity.From;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FromRepository extends JpaRepository<From, Long> {

    Optional<From> findByIdAndIsDeletedFalse(Long fromId);

    /**
     * 해당 유저의 모든 from 삭제
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UserFrom f WHERE f.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
