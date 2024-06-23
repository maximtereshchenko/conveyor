package com.github.maximtereshchenko.conveyor.plugin.api;

import java.nio.file.Path;
import java.util.Optional;

public interface ConveyorTask {

    String name();

    Optional<Path> execute();
}
