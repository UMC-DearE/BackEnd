package com.deare.backend.domain.letter.repository;

import com.deare.backend.domain.letter.entity.QLetter;
import com.deare.backend.domain.letter.entity.QLetterSearchToken;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;
import java.util.Set;

public class LetterSearchTokenRepositoryImpl implements LetterSearchTokenRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public LetterSearchTokenRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<Long> findCandidateLetterIds(
            Long userId,
            int indexKeyVersion,
            Set<String> tokens,
            int limit
    ) {
        if (userId == null || indexKeyVersion <= 0 || tokens == null || tokens.isEmpty() || limit <= 0) {
            return List.of();
        }

        QLetterSearchToken searchToken = QLetterSearchToken.letterSearchToken;
        QLetter letter = QLetter.letter;

        return queryFactory
                .select(searchToken.letter.id)
                .from(searchToken)
                .join(searchToken.letter, letter)
                .where(
                        letter.user.id.eq(userId),
                        letter.isDeleted.isFalse(),
                        searchToken.indexKeyVersion.eq(indexKeyVersion),
                        searchToken.token.in(tokens)
                )
                .groupBy(searchToken.letter.id)
                .having(searchToken.token.countDistinct().eq((long) tokens.size()))
                .limit(limit)
                .fetch();
    }
}
