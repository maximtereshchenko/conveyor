package com.github.maximtereshchenko.conveyor.plugin.clean;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

final class CleanTask implements ConveyorTask {

    @Override
    public Set<Product> execute(ConveyorSchematic schematic, Set<Product> products) {
        if (Files.exists(schematic.constructionDirectory())) {
            deleteRecursively(schematic.constructionDirectory());
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
