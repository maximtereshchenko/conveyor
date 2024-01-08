package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.JsonReader;
import java.nio.file.Path;

final class DirectoryRepository {

    private final Path directory;
    private final JsonReader jsonReader;

    DirectoryRepository(Path directory, JsonReader jsonReader) {
        this.directory = directory;
        this.jsonReader = jsonReader;
    }

    Path artifact(String name, int version) {
        return directory.resolve("%s-%d.jar".formatted(name, version));
    }

    ArtifactDefinition artifactDefinition(String name, int version) {
        return jsonReader.read(directory.resolve("%s-%d.json".formatted(name, version)), ArtifactDefinition.class);
    }
}
