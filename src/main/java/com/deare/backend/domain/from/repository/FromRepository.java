package com.deare.backend.domain.from.repository;

import com.deare.backend.domain.from.entity.From;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FromRepository extends JpaRepository<From, Long> {
    List<From> findAllByUserIdOrderByIdDesc(Long userId);
}
