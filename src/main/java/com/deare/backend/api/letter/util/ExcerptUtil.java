package com.deare.backend.api.letter.util;

import com.deare.backend.domain.letter.exception.LetterErrorCode;
import com.deare.backend.global.common.exception.GeneralException;

public final class ExcerptUtil {
    private ExcerptUtil() {}

    public static String excerptByChars(String content, int maxChars) {
        if (content == null) return null;
        if (maxChars < 1) {
            throw new GeneralException(LetterErrorCode.SUMMARY_INTERNAL_ERROR);
        }

        String normalized = content
                .replace("\r\n", " ")
                .replace("\n", " ")
                .replace("\t", " ")
                .trim();

        if (normalized.isEmpty()) return "";

        if (normalized.length() <= maxChars) return normalized;

        return normalized.substring(0, maxChars) + "...";
    }
}
