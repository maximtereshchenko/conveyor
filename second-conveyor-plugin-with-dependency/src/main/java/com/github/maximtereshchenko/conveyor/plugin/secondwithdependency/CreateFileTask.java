package com.github.maximtereshchenko.conveyor.plugin.secondwithdependency;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class CreateFileTask implements ConveyorTask {

    private final Path path;

    CreateFileTask(Path path) {
        this.path = path;
    }

    @Override
    public void execute() {
        try {
            Files.createFile(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
