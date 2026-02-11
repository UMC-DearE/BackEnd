package com.deare.backend.domain.report.entity;

import com.deare.backend.domain.report.entity.enums.ReportScope;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name="report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name="uq_user_scope_month",
                        columnNames = {"user_id", "scope", "report_year_month"}
                )
        }
)
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_report_id")
    private Long userReportId;

    @Enumerated(EnumType.STRING)
    @Column(name="scope", nullable = false)
    private ReportScope scope;

    @Column(name="report_year_month", nullable=false)
    private LocalDate yearMonth;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="recap_json", columnDefinition = "json")
    private String recapJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="top3_from_json", columnDefinition = "json")
    private String top3FromJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="top_phrase_json", columnDefinition = "json")
    private String topPhraseJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="emotion_dist_json", columnDefinition = "json")
    private String emotionDistJson;

    @Column(name="is_stale", nullable=false)
    private Boolean isStale=false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    public Report(User user, ReportScope scope, LocalDate yearMonth) {
        this.user = user;
        this.scope = scope;
        this.yearMonth = yearMonth;
        this.isStale = false;
    }

    public void markStale()
    {
        this.isStale=true;
    }

    public void markFresh()
    {
        this.isStale=false;
    }
}
