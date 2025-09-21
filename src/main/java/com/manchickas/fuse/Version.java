package com.manchickas.fuse;

import com.manchickas.optionated.result.Result;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.regex.Pattern;

public record Version(int major, int minor, int patch) implements Comparable<Version> {

    private static final Pattern PATTERN = Pattern.compile("^(?<major>\\d+)\\.(?<minor>\\d+)\\.(?<patch>\\d+)$");
    private static Version current;

    public synchronized static Result<Version> fetchCurrent() {
        if (Version.current == null) {
            try(var stream = Fuse.class.getClassLoader()
                    .getResourceAsStream("version.properties")) {
                var props = new Properties();
                props.load(stream);
                var version = props.getProperty("version");
                return Result.success(Version.current = Version.parse(version));
            } catch (IOException e) {
                return Result.error("Couldn't fetch the current version.");
            }
        }
        return Result.success(Version.current);
    }

    public static Result<Version> fetchLatest() {
        return Result.error("Not implemented.");
    }

    public static Version parse(String source) {
        var matcher = PATTERN.matcher(source);
        if (matcher.matches()) {
            var major = Integer.parseInt(matcher.group("major"));
            var minor = Integer.parseInt(matcher.group("minor"));
            var patch = Integer.parseInt(matcher.group("patch"));
            return new Version(major, minor, patch);
        }
        // Domain exception; Not a script one, so no ScriptException here.
        throw new IllegalArgumentException("Invalid version format");
    }

    @Override
    public int compareTo(@NotNull Version other) {
        if (this.major == other.major) {
            if (this.minor == other.minor)
                return Integer.compare(this.patch, other.patch);
            return Integer.compare(this.minor, other.minor);
        }
        return Integer.compare(this.major, other.major);
    }
}
