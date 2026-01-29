package com.deare.backend.api.folder.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FolderUpdateRequestDTO (
    @Size(min = 1, max = 6)
    @Pattern(regexp = ".*\\S.*", message = "name은 공백만으로 구성될 수 없습니다.")
    String name,
    Long imageId
){
    public boolean hasAnyField() {
        return name != null || imageId != null;
    }
}
