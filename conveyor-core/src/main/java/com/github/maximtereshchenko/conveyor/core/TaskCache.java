package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.files.Copy;
import com.github.maximtereshchenko.conveyor.files.FileTree;

import java.nio.file.Path;

final class TaskCache {

    private final Path directory;

    TaskCache(Path directory) {
        this.directory = directory;
    }

    boolean changed(Inputs inputs, Outputs outputs) {
        return changed(inputsChecksumPath(), inputs.checksum()) ||
               changed(outputsChecksumPath(), outputs.checksum());
    }

    boolean restore(Inputs inputs, Outputs outputs, Path destination) {
        var source = directory.resolve(String.valueOf(inputs.checksum()));
        var fileTree = new FileTree(source);
        if (!fileTree.exists()) {
            return false;
        }
        outputs.delete();
        fileTree.copyTo(destination);
        return true;
    }

    void store(Inputs inputs, Outputs outputs, Path root) {
        for (var path : outputs.paths()) {
            new FileTree(path)
                .walk(
                    new Copy(
                        path,
                        directory.resolve(String.valueOf(inputs.checksum()))
                            .resolve(root.relativize(path))
                    )
                );
        }
    }

    void remember(Inputs inputs, Outputs outputs) {
        new FileTree(inputsChecksumPath()).write(inputs.checksum());
        new FileTree(outputsChecksumPath()).write(outputs.checksum());
    }

    private boolean changed(Path path, long expected) {
        var fileTree = new FileTree(path);
        if (!fileTree.exists()) {
            return true;
        }
        return Long.parseLong(fileTree.read()) != expected;
    }

    private Path inputsChecksumPath() {
        return directory.resolve("inputs");
    }

    private Path outputsChecksumPath() {
        return directory.resolve("outputs");
    }
}
