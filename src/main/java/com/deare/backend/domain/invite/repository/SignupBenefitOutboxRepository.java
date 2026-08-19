package com.deare.backend.domain.invite.repository;

import com.deare.backend.domain.invite.entity.SignupBenefitOutbox;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SignupBenefitOutboxRepository extends JpaRepository<SignupBenefitOutbox, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select outbox from SignupBenefitOutbox outbox where outbox.id = :id")
    Optional<SignupBenefitOutbox> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select outbox.id
            from SignupBenefitOutbox outbox
            where outbox.status <> com.deare.backend.domain.invite.entity.enums.SignupBenefitOutboxStatus.COMPLETED
              and outbox.nextAttemptAt <= :now
            order by outbox.nextAttemptAt asc
            """)
    List<Long> findRetryableIds(@Param("now") LocalDateTime now, Pageable pageable);
}
