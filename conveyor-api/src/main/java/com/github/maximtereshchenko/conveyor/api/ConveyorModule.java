package com.github.maximtereshchenko.conveyor.api;

import java.nio.file.Path;

public interface ConveyorModule {

    BuildResult build(Path projectDefinition);
}
