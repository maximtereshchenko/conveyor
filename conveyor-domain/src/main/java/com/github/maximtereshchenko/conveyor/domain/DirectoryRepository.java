package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;

final class DirectoryRepository {

    private final Path directory;

    DirectoryRepository(Path directory) {
        this.directory = directory;
    }

    Path artifact(String name, int version) {
        return directory.resolve("%s-%d.jar".formatted(name, version));
    }
}
