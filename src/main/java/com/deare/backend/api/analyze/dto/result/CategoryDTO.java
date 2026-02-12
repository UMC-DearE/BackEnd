package com.deare.backend.api.analyze.dto.result;

import com.deare.backend.domain.emotion.entity.EmotionCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryDTO {
    private Long  categoryId;
    private String categoryType;
    private String bgColor;
    private String fontColor;

    public static CategoryDTO from(EmotionCategory category) {
        return CategoryDTO.builder()
                .categoryId(category.getId())
                .categoryType(category.getType())
                .bgColor(category.getBgColor())
                .fontColor(category.getFontColor())
                .build();

    }

}
