package com.deare.backend.domain.letter.repository.query.dto;

public interface FromLetterRankingProjection {
    Long getFromId();
    String getName();
    String getBgColor();
    String getFontColor();
    Long getLetterCount();
}
