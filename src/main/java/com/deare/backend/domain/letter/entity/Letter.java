package com.deare.backend.domain.letter.entity;

import com.deare.backend.domain.folder.entity.Folder;
import com.deare.backend.domain.from.entity.From;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name="letter")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Letter extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="letter_id")
    private Long id;

    @Lob
    @Column(name="content", nullable = false)
    private String content;

    @Column(name="received_at")
    private LocalDateTime receivedAt;

    @Column(name="ai_summary", nullable = false, length = 255)
    private String aiSummary;

    @Column(name="reply", length = 100)
    private String reply;

    @Column(name="is_liked", nullable = false)
    private boolean isLiked=false;

    @Column(name="is_pinned", nullable = false)
    private boolean isPinned=false;

    @Column(name="is_hidden", nullable = false)
    private boolean isHidden=false;

    @Column(name="content_version", nullable = false)
    private int contentVersion;

    @Column(name="content_hash", nullable = false, length = 64)
    private String contentHash;

    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_from_id", nullable = false)
    private From from;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_folder_id")
    private Folder folder;

    @OneToMany(
            mappedBy = "letter",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("imageOrder ASC")
    private List<LetterImage> letterImages=new ArrayList<>();


    public Letter(
            String content,
            LocalDateTime receivedAt,
            String aiSummary,
            int contentVersion,
            String contentHash,
            User user,
            From from,
            Folder folder
    ){
        this.content = content;
        this.receivedAt = receivedAt;
        this.aiSummary = aiSummary;
        this.contentVersion = contentVersion;
        this.contentHash = contentHash;
        this.user = user;
        this.from = from;
        this.folder = folder;
    }

    public void addLetterImage(LetterImage letterImage){
        letterImages.add(letterImage);
        letterImage.setLetter(this);
    }

    public void updateReply(String reply){
        this.reply = reply;
    }

    public void changeFolder(Folder folder){
        this.folder = folder;
    }
}
