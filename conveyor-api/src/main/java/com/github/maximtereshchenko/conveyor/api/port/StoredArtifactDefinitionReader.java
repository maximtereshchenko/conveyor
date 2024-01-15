package com.github.maximtereshchenko.conveyor.api.port;

import java.nio.file.Path;

public interface StoredArtifactDefinitionReader {

    StoredArtifactDefinition storedArtifactDefinition(Path path);
}
