package com.deare.backend.domain.from.repository.query;

import com.deare.backend.api.from.dto.FromResponseDTO;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FromQueryRepository {

    private final EntityManager em;

    public List<FromResponseDTO> findFromsWithLetterCount(Long userId) {
        return em.createQuery("""
                select new com.deare.backend.api.from.dto.FromResponseDTO(
                    f.id,
                    f.name,
                    f.backgroundColor,
                    f.fontColor,
                    count(l.id)
                )
                from UserFrom f
                left join Letter l
                    on l.from = f
                    and l.user.id = :userId
                    and l.isDeleted = false
                where f.user.id = :userId
                  and f.isDeleted = false
                group by f.id, f.name, f.backgroundColor, f.fontColor
                order by f.id desc
                """, FromResponseDTO.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
