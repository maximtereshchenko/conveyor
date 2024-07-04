package com.github.maximtereshchenko.conveyor.zip;

import com.github.maximtereshchenko.conveyor.files.FileTree;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;

public final class ZipArchive {

    private final Path path;

    public ZipArchive(Path path) {
        this.path = path;
    }

    public void extract(Path destination) {
        new FileTree(path).transfer(inputStream -> extract(inputStream, destination));
    }

    private void extract(InputStream inputStream, Path destination) throws IOException {
        try (var zipInputStream = new ZipInputStream(inputStream)) {
            for (
                var entry = zipInputStream.getNextEntry();
                entry != null;
                entry = zipInputStream.getNextEntry()
            ) {
                if (!entry.isDirectory()) {
                    new FileTree(destination.resolve(entry.getName()))
                        .write(zipInputStream::transferTo);
                }
            }
        }
    }
}
