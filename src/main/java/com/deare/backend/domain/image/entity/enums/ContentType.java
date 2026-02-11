package com.deare.backend.domain.image.entity.enums;

import com.deare.backend.domain.image.exception.ImageErrorCode;
import com.deare.backend.global.common.exception.GeneralException;

public enum ContentType {
    PROFILE("profile"),
    STICKER("sticker"),
    LETTER("letter"),
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
            throw new GeneralException(ImageErrorCode.IMAGE_40002);
        }

        return switch (dir.toLowerCase()) {
            case "profile" -> PROFILE;
            case "sticker" -> STICKER;
            case "folder" -> FOLDER;
            case "letter"  -> LETTER;
            default -> throw new GeneralException(ImageErrorCode.IMAGE_40001);
        };
    }
}
