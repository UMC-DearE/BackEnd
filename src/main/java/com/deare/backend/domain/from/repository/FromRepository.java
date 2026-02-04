package com.deare.backend.domain.from.repository;

import com.deare.backend.domain.from.entity.From;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FromRepository extends JpaRepository<From, Long> {

}
