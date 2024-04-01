package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

final class Properties {

    private static final Pattern INTERPOLATION_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private final Map<String, String> all;

    Properties(Map<String, String> all) {
        this.all = all;
    }

    String interpolated(String value) {
        return INTERPOLATION_PATTERN.matcher(value)
            .results()
            .reduce(
                value,
                (current, matchResult) ->
                    current.replace(
                        matchResult.group(),
                        interpolated(all.get(matchResult.group(1)))
                    ),
                (a, b) -> a
            );
    }

    Path remoteRepositoryCacheDirectory() {
        return path(SchematicPropertyKey.REMOTE_REPOSITORY_CACHE_DIRECTORY);
    }

    Path constructionDirectory() {
        return path(SchematicPropertyKey.CONSTRUCTION_DIRECTORY);
    }

    Optional<String> value(String key) {
        return Optional.ofNullable(interpolated(all.get(key)));
    }

    private Path path(SchematicPropertyKey schematicPropertyKey) {
        return Paths.get(all.get(schematicPropertyKey.fullName()));
    }
}
