package com.github.maximtereshchenko.conveyor.plugin.clean;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

final class CleanTask implements ConveyorTask {

    private final Path path;

    CleanTask(Path path) {
        this.path = path;
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        if (Files.exists(path)) {
            deleteRecursively(path);
        }
        return Set.of();
    }

    private void deleteRecursively(Path path) {
        try {
            Files.walkFileTree(path, new DeleteRecursivelyFileVisitor());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
