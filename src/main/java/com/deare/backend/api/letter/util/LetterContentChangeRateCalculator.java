package com.deare.backend.api.letter.util;

public final class LetterContentChangeRateCalculator {
    private LetterContentChangeRateCalculator() {}

    public static double calculateWordChangeRate(String oldContent, String newContent) {
        String[] oldWords = tokenize(oldContent);
        String[] newWords = tokenize(newContent);

        if (oldWords.length == 0) {
            return newWords.length == 0 ? 0.0 : 1.0;
        }

        int changedWordCount = wordLevelEditDistance(oldWords, newWords);

        return (double) changedWordCount / oldWords.length;
    }

    private static String[] tokenize(String text) {
        if (text == null) {
            return new String[0];
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return new String[0];
        }
        return trimmed.split("\\s+");
    }

    /** 단어 시퀀스 a, b 사이의 편집거리(삽입/삭제/치환 각 비용 1) — 단어 단위 Levenshtein. */
    private static int wordLevelEditDistance(String[] a, String[] b) {
        int n = a.length;
        int m = b.length;
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a[i - 1].equals(b[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                            Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[n][m];
    }
}
