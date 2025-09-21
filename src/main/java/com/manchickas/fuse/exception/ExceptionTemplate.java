package com.manchickas.fuse.exception;

import java.util.regex.Pattern;

public final class ExceptionTemplate {

    private static final Pattern ARGUMENT = Pattern.compile("\\{([0-9]+)?}");
    private final String pattern;

    ExceptionTemplate(String pattern) {
        this.pattern = pattern;
    }

    private static String format(String pattern, Object... args) {
        var matcher = ARGUMENT.matcher(pattern);
        var builder = new StringBuilder();
        int last = 0, argument = 0;
        while (matcher.find()) {
            builder.append(pattern, last, matcher.start());
            last = matcher.end();
            if (matcher.group(1) != null) {
                var index = Integer.parseInt(matcher.group(1));
                if (index < args.length)
                    builder.append(args[index]);
                continue;
            }
            builder.append(args[argument++]);
        }
        if (last < pattern.length())
            builder.append(pattern, last, pattern.length());
        return builder.toString();
    }

    public String message(Object... args) {
        return ExceptionTemplate.format(this.pattern, args);
    }

    public ScriptException build(Object... args) {
        return new ScriptException(ExceptionTemplate.format(this.pattern, args));
    }
}
