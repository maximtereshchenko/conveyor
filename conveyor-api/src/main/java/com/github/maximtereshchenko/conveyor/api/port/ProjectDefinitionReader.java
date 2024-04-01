package com.github.maximtereshchenko.conveyor.api.port;

import java.nio.file.Path;

public interface ProjectDefinitionReader {

    ProjectDefinition projectDefinition(Path path);
}
