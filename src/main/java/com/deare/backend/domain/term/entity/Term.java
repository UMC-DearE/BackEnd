package com.deare.backend.domain.term.entity;

import com.deare.backend.domain.term.entity.enums.TermType;
import com.deare.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "term")
public class Term extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "terms_id")
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TermType type;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "effective_at", nullable = false)
    private LocalDateTime effectiveAt;

    @Column(name = "version", nullable = false, length = 100)
    private String version = "1.0";

    // 테스트용 생성자
    public Term(String title, TermType type, String content, boolean isRequired) {
        this.title = title;
        this.type = type;
        this.content = content;
        this.isRequired = isRequired;
        this.effectiveAt = LocalDateTime.now();
    }
}
