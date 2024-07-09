package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.files.FileTree;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

final class LocalDirectoryRepository extends UriRepository<Resource, Path> {

    LocalDirectoryRepository(Path path) {
        super(path.toUri());
    }

    @Override
    void publish(URI uri, Resource resource) {
        var fileTree = new FileTree(Paths.get(uri));
        if (fileTree.exists()) {
            return;
        }
        resource.transferTo(fileTree);
    }

    @Override
    Optional<Path> artifact(URI uri) {
        var requested = Paths.get(uri);
        if (Files.exists(requested)) {
            return Optional.of(requested);
        }
        return Optional.empty();
    }
}
