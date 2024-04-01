package com.github.maximtereshchenko.conveyor.api.exception;

import java.nio.file.Path;

public final class CouldNotFindProjectDefinition extends RuntimeException {

    public CouldNotFindProjectDefinition(Path path) {
        super(path.toString());
    }
}
