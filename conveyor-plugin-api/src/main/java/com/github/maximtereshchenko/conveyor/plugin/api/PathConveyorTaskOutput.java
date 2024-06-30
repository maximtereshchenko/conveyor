package com.github.maximtereshchenko.conveyor.plugin.api;

import java.nio.file.Path;

public record PathConveyorTaskOutput(Path path) implements ConveyorTaskOutput {

    @Override
    public int compareTo(ConveyorTaskOutput output) {
        return switch (output) {
            case PathConveyorTaskOutput pathOutput -> path.compareTo(pathOutput.path());
        };
    }
}
