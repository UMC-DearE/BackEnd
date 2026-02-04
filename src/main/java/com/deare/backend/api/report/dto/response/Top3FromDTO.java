package com.deare.backend.api.report.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Top3FromDTO {
    private int rank;
    private String name;
    private int count;

    public static Top3FromDTO of(int rank, String name, int count) {
        return Top3FromDTO.builder()
                .rank(rank)
                .name(name)
                .count(count)
                .build();
    }
}
