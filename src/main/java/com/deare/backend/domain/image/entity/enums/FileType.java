package com.deare.backend.domain.image.entity.enums;

import com.deare.backend.domain.image.exception.ImageErrorCode;
import com.deare.backend.global.common.exception.GeneralException;

public enum FileType {
    JPG, JPEG, PNG;

    public static FileType from(String originalFileName) {
        if (originalFileName == null || !originalFileName.contains(".")) {
            throw new GeneralException(ImageErrorCode.IMAGE_40001);
        }

        String ext = originalFileName
                .substring(originalFileName.lastIndexOf('.') + 1)
                .toUpperCase();

        try {
            return FileType.valueOf(ext);
        } catch (IllegalArgumentException e) {
            // jpg / jpeg / png 외 확장자
            throw new GeneralException(ImageErrorCode.IMAGE_40001);
        }
    }
}
