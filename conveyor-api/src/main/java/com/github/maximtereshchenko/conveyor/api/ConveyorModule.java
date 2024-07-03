package com.github.maximtereshchenko.conveyor.api;

import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;
import java.util.List;

public interface ConveyorModule {

    void construct(Path path, List<Stage> stages);
}
