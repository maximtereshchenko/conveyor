package com.github.maximtereshchenko.conveyor.zip;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.files.Generic;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipArchiveContainer {

    private final Path path;

    public ZipArchiveContainer(Path path) {
        this.path = path;
    }

    public void archive(Path destination) {
        new FileTree(destination).write(this::write);
    }

    private void write(OutputStream outputStream) throws IOException {
        try (var zipOutputStream = new ZipOutputStream(outputStream)) {
            new FileTree(path).walk(new Generic(file -> write(file, path, zipOutputStream)));
        }
    }

    private void write(Path file, Path base, ZipOutputStream zipOutputStream) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(base.relativize(file).toString()));
        new FileTree(file).read(inputStream -> inputStream.transferTo(zipOutputStream));
        zipOutputStream.closeEntry();
    }
}
