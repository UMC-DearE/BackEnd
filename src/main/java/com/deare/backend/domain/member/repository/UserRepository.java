package com.deare.backend.domain.member.repository;

import com.deare.backend.domain.member.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
