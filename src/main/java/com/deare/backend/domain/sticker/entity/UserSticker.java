package com.deare.backend.domain.sticker.entity;

import com.deare.backend.domain.image.entity.Image;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_sticker")
public class UserSticker extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_sticker_id")
    private Long id;

    @Column(name = "pos_x", nullable = false, precision = 5, scale = 2)
    private BigDecimal posX;

    @Column(name = "pos_y", nullable = false, precision = 5, scale = 2)
    private BigDecimal posY;

    @Column(name = "pos_z", nullable = false)
    private int posZ;

    @Column(name = "rotation", nullable = false, precision = 6, scale = 2)
    private BigDecimal rotation;

    @Column(name = "scale", nullable = false, precision = 3, scale = 2)
    private BigDecimal scale;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static UserSticker create(
            User user,
            Image image,
            BigDecimal posX,
            BigDecimal posY,
            int posZ,
            BigDecimal rotation,
            BigDecimal scale
    ) {
        UserSticker us = new UserSticker();
        us.user = user;
        us.image = image;
        us.posX = posX;
        us.posY = posY;
        us.posZ = posZ;
        us.rotation = rotation;
        us.scale = scale;
        return us;
    }
    public boolean isOwnedBy(Long userId) {
        return user != null && user.getId() != null && user.getId().equals(userId);
    }

    public void updateTransform(
            BigDecimal posX,
            BigDecimal posY,
            int posZ,
            BigDecimal rotation,
            BigDecimal scale
    ) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.rotation = rotation;
        this.scale = scale;
    }

}
