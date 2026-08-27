package com.deare.backend.domain.letter.search;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class LetterSearchBigramGenerator {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

    private LetterSearchBigramGenerator() {
    }

    public static String normalize(String text) {
        if (text == null) return "";

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);

        return WHITESPACE.matcher(normalized).replaceAll(" ").trim();
    }

    public static List<String> generateUnique(String text) {
        int[] codePoints = normalize(text).codePoints().toArray();
        if (codePoints.length < 2) return List.of();

        Set<String> bigrams = new LinkedHashSet<>();
        for (int i = 0; i < codePoints.length - 1; i++) {
            bigrams.add(new String(codePoints, i, 2));
        }

        return List.copyOf(bigrams);
    }
}
