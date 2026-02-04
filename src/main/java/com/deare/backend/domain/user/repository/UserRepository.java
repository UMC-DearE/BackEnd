package com.deare.backend.domain.user.repository;

import com.deare.backend.domain.user.entity.Provider;
import com.deare.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    Optional<User> findByIdAndIsDeletedFalse(Long userId);
}
