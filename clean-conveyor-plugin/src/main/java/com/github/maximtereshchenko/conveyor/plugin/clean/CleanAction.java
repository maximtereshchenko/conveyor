package com.github.maximtereshchenko.conveyor.plugin.clean;

import com.github.maximtereshchenko.conveyor.files.FileTree;

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
            new FileTree(path).delete();
            LOGGER.log(System.Logger.Level.INFO, "Removed {0}", path);
        } else {
            LOGGER.log(System.Logger.Level.WARNING, "{0} does not exist", path);
        }
        return Optional.empty();
    }
}
