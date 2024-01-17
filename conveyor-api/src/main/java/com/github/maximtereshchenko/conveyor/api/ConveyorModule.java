package com.github.maximtereshchenko.conveyor.api;

import com.github.maximtereshchenko.conveyor.common.api.BuildFiles;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import java.nio.file.Path;

public interface ConveyorModule {

    BuildFiles build(Path projectDefinition, Stage stage);
}
