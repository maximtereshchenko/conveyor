package com.github.maximtereshchenko.conveyor.common.api;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class BuildFiles {

    private final Map<Path, BuildFileType> files;

    private BuildFiles(Map<Path, BuildFileType> files) {
        this.files = Map.copyOf(files);
    }

    public BuildFiles() {
        this(Map.of());
    }

    public Collection<Path> byType(BuildFileType type) {
        return files.entrySet()
            .stream()
            .filter(entry -> entry.getValue() == type)
            .map(Map.Entry::getKey)
            .toList();
    }

    public BuildFiles with(Path path, BuildFileType type) {
        var copy = new HashMap<>(files);
        copy.put(path, type);
        return new BuildFiles(copy);
    }
}
