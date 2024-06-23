package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

final class Properties {

    private static final Pattern INTERPOLATION_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private final PropertiesModel propertiesModel;

    Properties(PropertiesModel propertiesModel) {
        this.propertiesModel = propertiesModel;
    }

    String interpolated(String value) {
        return INTERPOLATION_PATTERN.matcher(value)
            .results()
            .reduce(
                value,
                (current, matchResult) ->
                    current.replace(
                        matchResult.group(),
                        value(matchResult.group(1))
                            .map(this::interpolated)
                            .orElse("")
                    ),
                (a, b) -> a
            );
    }

    Path remoteRepositoryCacheDirectory() {
        return propertiesModel.path(SchematicPropertyKey.REMOTE_REPOSITORY_CACHE_DIRECTORY);
    }

    Optional<String> value(String key) {
        return propertiesModel.value(key)
            .map(this::interpolated);
    }
}
