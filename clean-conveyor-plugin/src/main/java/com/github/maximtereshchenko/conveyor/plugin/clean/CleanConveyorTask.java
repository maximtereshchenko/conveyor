package com.github.maximtereshchenko.conveyor.plugin.clean;

import com.github.maximtereshchenko.conveyor.common.api.BuildFiles;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class CleanConveyorTask implements ConveyorTask {

    private final Path directory;

    CleanConveyorTask(Path directory) {
        this.directory = directory;
    }

    @Override
    public BuildFiles execute(BuildFiles buildFiles) {
        if (!Files.exists(directory)) {
            return buildFiles;
        }
        try {
            Files.walkFileTree(directory, new DeleteDirectoryRecursively());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return buildFiles;
    }
}
