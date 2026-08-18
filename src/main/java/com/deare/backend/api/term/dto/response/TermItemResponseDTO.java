package com.deare.backend.api.term.dto.response;

import com.deare.backend.domain.term.entity.Term;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record TermItemResponseDTO(
        Long termId,
        String title,
        String type,
        List<TermClauseDTO> clauses,
        boolean isRequired,
        LocalDateTime effectiveAt,
        String version
) {
    private static final Pattern ARTICLE_PATTERN =
            Pattern.compile("제\\s*\\d+조[^\\n]*", Pattern.MULTILINE);

    private static final Pattern NUMBERED_PATTERN =
            Pattern.compile("^\\d+\\.\\s*.+", Pattern.MULTILINE);

    public static TermItemResponseDTO from(Term term) {
        return new TermItemResponseDTO(
                term.getId(),
                term.getTitle(),
                term.getType().name(),
                parseClauses(term.getContent()),
                term.isRequired(),
                term.getEffectiveAt(),
                term.getVersion()
        );
    }

    private static List<TermClauseDTO> parseClauses(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        Pattern headerPattern = ARTICLE_PATTERN.matcher(content).find()
                ? ARTICLE_PATTERN
                : NUMBERED_PATTERN;

        Matcher matcher = headerPattern.matcher(content);
        List<int[]> headers = new ArrayList<>();
        while (matcher.find()) {
            headers.add(new int[]{matcher.start(), matcher.end()});
        }

        if (headers.isEmpty()) {
            return List.of(new TermClauseDTO("", content.strip()));
        }

        List<TermClauseDTO> clauses = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            int titleStart = headers.get(i)[0];
            int titleEnd   = headers.get(i)[1];
            int bodyEnd    = (i + 1 < headers.size()) ? headers.get(i + 1)[0] : content.length();
            clauses.add(new TermClauseDTO(
                    content.substring(titleStart, titleEnd).strip(),
                    content.substring(titleEnd, bodyEnd).strip()
            ));
        }
        return clauses;
    }
}
