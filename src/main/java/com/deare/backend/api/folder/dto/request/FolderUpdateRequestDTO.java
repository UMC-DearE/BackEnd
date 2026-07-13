package com.deare.backend.api.folder.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FolderUpdateRequestDTO (
    @Size(min = 1, max = 6)
    @Pattern(regexp = ".*\\S.*", message = "name은 공백만으로 구성될 수 없습니다.")
    String name,
    Long imageId,
    FolderImageAction imageAction
){
    public boolean hasAnyField() {
        return name != null || imageAction != null;
    }

    public boolean hasInvalidImageRequest() {
        if (imageAction == null || imageAction == FolderImageAction.KEEP) {
            return imageId != null;
        }

        if (imageAction == FolderImageAction.CHANGE) {
            return imageId == null;
        }

        return imageId != null;
    }
}
