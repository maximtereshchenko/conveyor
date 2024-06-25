package com.github.maximtereshchenko.conveyor.plugin.clean;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

final class CleanAction implements Supplier<Optional<Path>> {

    private static final System.Logger LOGGER = System.getLogger(CleanAction.class.getName());

    private final Path path;

    CleanAction(Path path) {
        this.path = path;
    }

    @Override
    public Optional<Path> get() {
        if (Files.exists(path)) {
            deleteRecursively(path);
            LOGGER.log(System.Logger.Level.INFO, "Removed {0}", path);
        } else {
            LOGGER.log(System.Logger.Level.WARNING, "{0} does not exist", path);
        }
        return Optional.empty();
    }

    private void deleteRecursively(Path path) {
        try {
            Files.walkFileTree(path, new DeleteRecursivelyFileVisitor());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
