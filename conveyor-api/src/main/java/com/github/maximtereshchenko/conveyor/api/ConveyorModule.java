package com.github.maximtereshchenko.conveyor.api;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import java.nio.file.Path;

public interface ConveyorModule {

    BuildResult build(Path projectDefinition, Stage stage);
}
