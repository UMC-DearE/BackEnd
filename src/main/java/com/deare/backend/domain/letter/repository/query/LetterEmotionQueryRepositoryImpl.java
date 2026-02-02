package com.deare.backend.domain.letter.repository.query;

import com.deare.backend.domain.letter.repository.query.dto.EmotionCategoryProjection;
import com.deare.backend.domain.letter.repository.query.dto.EmotionTagProjection;
import com.deare.backend.domain.emotion.entity.QEmotion;
import com.deare.backend.domain.emotion.entity.QEmotionCategory;
import com.deare.backend.domain.emotion.entity.QLetterEmotion;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LetterEmotionQueryRepositoryImpl
        implements LetterEmotionQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<EmotionTagProjection> findEmotionTagsByLetterId(Long letterId) {
        if (letterId == null) return List.of();

        QLetterEmotion le = QLetterEmotion.letterEmotion;
        QEmotion e = QEmotion.emotion;
        QEmotionCategory c = QEmotionCategory.emotionCategory;

        return queryFactory
                .select(Projections.constructor(
                        EmotionTagProjection.class,
                        e.id,
                        e.name,
                        Projections.constructor(
                                EmotionCategoryProjection.class,
                                c.id,
                                c.type,
                                c.bgColor,
                                c.fontColor
                        )
                ))
                .from(le)
                .join(le.emotion, e)
                .join(e.emotionCategory, c)
                .where(le.letter.id.eq(letterId))
                .orderBy(
                        c.id.asc(),
                        e.id.asc()
                )
                .fetch();
    }
}