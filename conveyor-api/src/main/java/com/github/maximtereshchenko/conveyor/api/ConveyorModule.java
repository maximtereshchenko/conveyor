package com.github.maximtereshchenko.conveyor.api;

import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;

public interface ConveyorModule {

    SchematicProducts construct(Path path, Stage stage);
}
