package com.github.maximtereshchenko.compiler;

import javax.tools.StandardJavaFileManager;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

final class SecondaryFileSystemPathFactory implements StandardJavaFileManager.PathFactory {

    private final FileSystem fileSystem;

    SecondaryFileSystemPathFactory(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public Path getPath(String first, String... more) {
        var primary = Paths.get(first, more);
        if (Files.exists(primary)) {
            return primary;
        }
        return fileSystem.getPath(first, more);
    }
}
