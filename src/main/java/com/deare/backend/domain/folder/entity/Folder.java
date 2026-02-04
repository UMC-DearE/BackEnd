package com.deare.backend.domain.folder.entity;

import com.deare.backend.domain.image.entity.Image;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="user_folder")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Folder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_folder_id")
    private Long id;

    @Column(name="folder_name", nullable = false, length = 6)
    private String name;

    @Column(name="folder_order", nullable = false)
    private int folderOrder;

    /***
     * 이미지테이블과 1:1 연관관계
     * 일단은 Long으로 설정해둠
     * 엔티티 합칠때 수정 필요
     */
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name="image_id")
    private Image image;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    private Folder(
            String name,
            int order,
            Image image,
            User user
    ){
        this.name = name;
        this.folderOrder = order;
        this.image = image;
        this.user = user;
    }

    public static Folder create(
            String name,
            int order,
            Image image,
            User user
    ){
        if (order < 1) {
            throw new IllegalArgumentException("folderOrder는 1 이상의 정수여야 합니다.");
        }

        return new Folder(name, order, image, user);
    }

    public void rename(String name){
        this.name = name;
    }

    public void changeOrder(int order){
        this.folderOrder = order;
    }

    public void changeImage(Image image){
        this.image = image;
    }
}
