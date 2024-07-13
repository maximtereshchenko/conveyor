package com.github.maximtereshchenko.conveyor.api;

import java.nio.file.Path;
import java.util.List;

public interface ConveyorModule {

    void construct(Path path, List<Stage> stages);
}
