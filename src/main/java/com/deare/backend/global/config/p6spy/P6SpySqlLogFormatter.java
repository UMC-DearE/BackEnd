package com.deare.backend.global.config.p6spy;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.FormatStyle;

import java.util.Locale;
import java.util.Stack;
import java.util.function.Predicate;

import static java.util.Arrays.stream;

public class P6SpySqlLogFormatter implements MessageFormattingStrategy {

    private static final String NEW_LINE = System.lineSeparator();
    private static final String P6SPY_FORMATTER = "P6SpySqlLogFormatter";
    private static final String APP_PACKAGE = "com.deare.backend";
    private static final String CREATE = "create";
    private static final String ALTER = "alter";
    private static final String COMMENT = "comment";
    private static final long STACKTRACE_THRESHOLD_MS = 200;

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared,
                                String sql, String url) {
        // 실행 시간이 STACKTRACE_THRESHOLD_MS ms 이상일 때만 콜스택 출력
        StringBuilder callStack = (elapsed >= STACKTRACE_THRESHOLD_MS) ? getStackBuilder() : new StringBuilder("(skip stack)");
        return formatSql(sql, category, getMessage(connectionId, elapsed, callStack));
    }

    private String formatSql(String sql, String category, String message) {
        if (sql == null || sql.trim().isEmpty()) {
            return NEW_LINE + "[P6Spy] (empty sql)" + message;
        }
        return NEW_LINE
                + formatSql(sql, category)
                + message;
    }

    private String formatSql(String sql, String category) {
        if (isStatementDDL(sql, category)) {
            return FormatStyle.DDL
                    .getFormatter()
                    .format(sql);
        }
        return FormatStyle.BASIC
                .getFormatter()
                .format(sql);
    }

    private boolean isStatementDDL(String sql, String category) {
        return isStatement(category) && isDDL(sql.trim().toLowerCase(Locale.ROOT));
    }

    private boolean isStatement(String category) {
        return Category.STATEMENT.getName().equals(category);
    }

    private boolean isDDL(String lowerSql) {
        return lowerSql.startsWith(CREATE) || lowerSql.startsWith(ALTER) || lowerSql.startsWith(COMMENT);
    }

    private String getMessage(int connectionId, long elapsed, StringBuilder callStackBuilder) {
        return NEW_LINE
                + NEW_LINE
                + "\t" + String.format("Connection ID: %s", connectionId)
                + NEW_LINE
                + "\t" + String.format("Execution Time: %s ms", elapsed)
                + NEW_LINE
                + NEW_LINE
                + "\t" + String.format("Call Stack (number 1 is entry point): %s", callStackBuilder)
                + NEW_LINE
                + NEW_LINE
                + "----------------------------------------------------------------------------------------------------";
    }

    private StringBuilder getStackBuilder() {
        Stack<String> callStack = new Stack<>();
        StackTraceElement[] traces = new Throwable().getStackTrace();

        stream(traces)
                .map(StackTraceElement::toString)
                .filter(shouldIncludeInCallStack())
                .forEach(callStack::push);

        // fallback: app 프레임이 없으면 원본 상위 5줄
        if (callStack.isEmpty()) {
            StringBuilder fb = new StringBuilder("[P6Spy] (no app frames)");
            int limit = Math.min(5, traces.length);
            for (int i = 0; i < limit; i++) {
                fb.append(NEW_LINE)
                        .append("\t\t")
                        .append(i + 1)
                        .append(". ")
                        .append(traces[i]);
            }
            return fb;
        }

        int order = 1;
        StringBuilder callStackBuilder = new StringBuilder();
        while (!callStack.empty()) {
            callStackBuilder.append(NEW_LINE)
                    .append("\t\t")
                    .append(order++)
                    .append(". ")
                    .append(callStack.pop());
        }
        return callStackBuilder;
    }

    private Predicate<String> shouldIncludeInCallStack() {
        return frame -> frame.startsWith(APP_PACKAGE) && !frame.contains(P6SPY_FORMATTER);
    }
}