package com.manchickas.fuse;

import com.manchickas.optionated.option.Option;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public record Version(int major, int minor, int patch) implements Comparable<Version> {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final URI REPOSITORY = URI.create("https://raw.githubusercontent.com/Manchick0/Fuse/refs/heads/master/gradle.properties");
    private static final Pattern PATTERN = Pattern.compile("^(?<major>\\d+)\\.(?<minor>\\d+)\\.(?<patch>\\d+)$");

    public static void main(String[] args) {
        System.out.println(Version.fetchLatest().join());
    }

    public static CompletableFuture<Option<Version>> fetchLatest() {
        var request = HttpRequest.newBuilder(REPOSITORY).GET()
                .build();
        return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try(var stream = response.body()) {
                            var props = new Properties();
                            props.load(stream);
                            var version = props.getProperty("latest");
                            return Version.parse(version);
                        } catch (IOException ignored) {
                            // Fallthrough here
                        }
                    }
                    return Option.none();
                });
    }

    public static Option<Version> parse(String source) {
        var matcher = PATTERN.matcher(source);
        if (matcher.matches()) {
            var major = Integer.parseInt(matcher.group("major"));
            var minor = Integer.parseInt(matcher.group("minor"));
            var patch = Integer.parseInt(matcher.group("patch"));
            return Option.some(new Version(major, minor, patch));
        }
        return Option.none();
    }

    @Override
    public @NotNull String toString() {
        return String.format("%d.%d.%d", this.major, this.minor, this.patch);
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
