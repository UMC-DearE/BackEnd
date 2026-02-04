package com.deare.backend.domain.image.entity;

import com.deare.backend.domain.image.exception.ImageErrorCode;
import com.deare.backend.global.common.exception.GeneralException;

public enum ContentType {
    PROFILE("profile"),
    STICKER("sticker"),
    FOLDER("folder");

    private final String dirName;

    ContentType(String dirName) {
        this.dirName = dirName;
    }

    public String getDirName() {
        return dirName;
    }

    public static ContentType from(String dir) {
        if (dir == null || dir.isBlank()) {
            return FOLDER;
        }

        return switch (dir.toLowerCase()) {
            case "profile" -> PROFILE;
            case "sticker" -> STICKER;
            case "folder", "letter" -> FOLDER;
            default -> throw new GeneralException(ImageErrorCode.IMAGE_40001);
        };
    }
}
