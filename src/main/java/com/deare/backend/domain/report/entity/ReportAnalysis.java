package com.deare.backend.domain.report.entity;

import com.deare.backend.domain.user.entity.User;
import com.deare.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "report_analysis",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_report_analysis_user",
                        columnNames = {"user_id"}
                )
        }
)
public class ReportAnalysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_analysis_id")
    private Long id;

    @Column(name = "description", nullable = false, length = 200)
    private String description;

    @Column(name = "hashtag1", nullable = false, length = 50)
    private String hashtag1;

    @Column(name = "hashtag2", nullable = false, length = 50)
    private String hashtag2;

    @Column(name = "analyzed_letter_count", nullable = false)
    private int analyzedLetterCount;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ReportAnalysis(
            User user,
            String description,
            String hashtag1,
            String hashtag2,
            int analyzedLetterCount,
            LocalDateTime analyzedAt
    ) {
        this.user = user;
        this.description = description;
        this.hashtag1 = hashtag1;
        this.hashtag2 = hashtag2;
        this.analyzedLetterCount = analyzedLetterCount;
        this.analyzedAt = analyzedAt;
    }


    public void reanalyze(
            String description,
            String hashtag1,
            String hashtag2,
            int analyzedLetterCount,
            LocalDateTime analyzedAt
    ) {
        this.description = description;
        this.hashtag1 = hashtag1;
        this.hashtag2 = hashtag2;
        this.analyzedLetterCount = analyzedLetterCount;
        this.analyzedAt = analyzedAt;
    }
}
