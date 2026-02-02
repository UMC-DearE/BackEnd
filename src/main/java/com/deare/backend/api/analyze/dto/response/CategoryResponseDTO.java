package com.deare.backend.api.analyze.dto.response;

import com.deare.backend.domain.emotion.entity.EmotionCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponseDTO {
    private Long  categoryId;
    private String categoryType;
    private String bgColor;
    private String fontColor;

    public static CategoryResponseDTO from(EmotionCategory category) {
        return CategoryResponseDTO.builder()
                .categoryId(category.getId())
                .categoryType(category.getType())
                .bgColor(category.getBgColor())
                .fontColor(category.getFontColor())
                .build();

    }

}
