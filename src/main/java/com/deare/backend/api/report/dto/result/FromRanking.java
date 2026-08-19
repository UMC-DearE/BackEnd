package com.deare.backend.api.report.dto.result;

import com.deare.backend.domain.from.entity.From;

public record FromRanking(
        int rank,
        String name,
        int count,
        String bgColor,
        String fontColor
) {
    public static FromRanking of(
        int rank,
        String name,
        int count,
        String bgColor,
        String fontColor
    ){
        return new FromRanking(rank, name, count, bgColor, fontColor);
    }
}
