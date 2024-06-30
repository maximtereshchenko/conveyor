package com.github.maximtereshchenko.conveyor.plugin.api;

import java.nio.file.Path;

public record PathConveyorTaskInput(Path path) implements ConveyorTaskInput {

    @Override
    public int compareTo(ConveyorTaskInput input) {
        return switch (input) {
            case PathConveyorTaskInput pathInput -> path.compareTo(pathInput.path());
        };
    }
}
