package com.deare.backend.domain.letter.repository;

import com.deare.backend.domain.image.entity.QImage;
import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.entity.QLetter;
import com.deare.backend.domain.folder.entity.QFolder;
import com.deare.backend.domain.from.entity.QFrom;
import com.deare.backend.domain.letter.entity.QLetterImage;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public class LetterRepositoryImpl implements LetterRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public LetterRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<Letter> findLettersForList(
            Long userId,
            Long folderId,
            Long fromId,
            String keyword,
            Pageable pageable
    ) {
        QLetter letter = QLetter.letter;
        QFrom from = QFrom.from;
        QFolder folder = QFolder.folder;

        JPAQuery<Letter> contentQuery = queryFactory
                .selectFrom(letter)
                .join(letter.from, from).fetchJoin()
                .leftJoin(letter.folder, folder).fetchJoin()
                .where(
                        ownedBy(letter, userId),
                        folderIdEq(letter, folderId),
                        fromIdEq(letter, fromId),
                        keywordLike(letter, keyword)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<OrderSpecifier<?>> orderSpecifiers = toOrderSpecifiers(pageable.getSort(), letter);
        if (!orderSpecifiers.isEmpty()) {
            contentQuery.orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new));
        } else {
            contentQuery.orderBy(letter.id.desc());
        }

        List<Letter> contents = contentQuery.fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(letter.count())
                .from(letter)
                .where(
                        ownedBy(letter, userId),
                        folderIdEq(letter, folderId),
                        fromIdEq(letter, fromId),
                        keywordLike(letter, keyword)
                );

        return PageableExecutionUtils.getPage(contents, pageable, () -> {
            Long cnt = countQuery.fetchOne();
            return cnt == null ? 0L : cnt;
        });
    }

    @Override
    public Optional<Letter> findLetterDetailById(Long userId, Long letterId) {
        QLetter letter = QLetter.letter;
        QFrom from = QFrom.from;
        QFolder folder = QFolder.folder;
        QLetterImage letterImage = QLetterImage.letterImage;
        QImage image = QImage.image;

        Letter result = queryFactory
                .selectFrom(letter)
                .distinct()
                .join(letter.from, from).fetchJoin()
                .leftJoin(letter.folder, folder).fetchJoin()
                .leftJoin(letter.letterImages, letterImage).fetchJoin()
                .leftJoin(letterImage.image, image).fetchJoin()
                .where(
                        letter.id.eq(letterId),
                        letter.user.id.eq(userId)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }


    private BooleanExpression ownedBy(QLetter letter, Long userId) {
        if (userId == null) return null;

        return letter.user.id.eq(userId);
    }

    private BooleanExpression folderIdEq(QLetter letter, Long folderId) {
        if (folderId == null) return null;
        return letter.folder.id.eq(folderId);
    }

    private BooleanExpression fromIdEq(QLetter letter, Long fromId) {
        if (fromId == null) return null;
        return letter.from.id.eq(fromId);
    }

    private BooleanExpression keywordLike(QLetter letter, String keyword) {
        if (!StringUtils.hasText(keyword)) return null;
        return letter.content.containsIgnoreCase(keyword.trim());
    }

    private List<OrderSpecifier<?>> toOrderSpecifiers(Sort sort, QLetter letter) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        if (sort == null || sort.isUnsorted()) return orders;

        for (Sort.Order o : sort) {
            String property = o.getProperty();

            if (!isAllowedSort(property)) continue;

            Order direction = o.isAscending() ? Order.ASC : Order.DESC;

            ComparableExpressionBase<?> path =
                    Expressions.comparablePath(Comparable.class, letter, property);

            orders.add(new OrderSpecifier<>(direction, path));
        }

        return orders;
    }

    private boolean isAllowedSort(String property) {
        return property.equals("id")
                || property.equals("createdAt")
                || property.equals("receivedAt")
                || property.equals("isLiked")
                || property.equals("isPinned");
    }
}
