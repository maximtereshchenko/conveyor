package com.github.maximtereshchenko.conveyor.plugin.resources;

import com.github.maximtereshchenko.conveyor.files.FileTree;

import java.nio.file.Files;
import java.nio.file.Path;

final class CopyResourcesAction implements Runnable {

    private final Path resourcesDirectory;
    private final Path classesDirectory;

    CopyResourcesAction(Path resourcesDirectory, Path classesDirectory) {
        this.resourcesDirectory = resourcesDirectory;
        this.classesDirectory = classesDirectory;
    }

    @Override
    public void run() {
        if (Files.exists(resourcesDirectory) && Files.exists(classesDirectory)) {
            new FileTree(resourcesDirectory).copyTo(classesDirectory);
        }
    }
}
