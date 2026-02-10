package com.deare.backend.api.letter.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Getter
public class LetterUpdateRequestDTO {

    @Size(max = 5000)
    private String content;

    private LocalDate receivedAt;

    @JsonIgnore
    private boolean receivedAtSpecified;

    private Long fromId;

    @JsonSetter("receivedAt")
    public void setReceivedAt(LocalDate receivedAt) {
        this.receivedAt = receivedAt;
        this.receivedAtSpecified = true;
    }

    public boolean hasAnyField() {
        return StringUtils.hasText(content)
                || receivedAtSpecified
                || fromId != null;
    }
}
