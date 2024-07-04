package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.files.IOSupplier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

final class Resource {

    private final IOSupplier<InputStream> supplier;

    Resource(IOSupplier<InputStream> supplier) {
        this.supplier = supplier;
    }

    Resource(Path path) {
        this(() -> Files.newInputStream(path));
    }

    void transferTo(FileTree fileTree) {
        fileTree.write(outputStream -> {
            try (var inputStream = supplier.get()) {
                inputStream.transferTo(outputStream);
            }
        });
    }
}
