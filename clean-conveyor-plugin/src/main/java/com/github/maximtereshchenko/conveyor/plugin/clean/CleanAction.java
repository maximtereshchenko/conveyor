package com.github.maximtereshchenko.conveyor.plugin.clean;

import com.github.maximtereshchenko.conveyor.files.FileTree;

import java.nio.file.Path;

final class CleanAction implements Runnable {

    private static final System.Logger LOGGER = System.getLogger(CleanAction.class.getName());

    private final Path path;

    CleanAction(Path path) {
        this.path = path;
    }

    @Override
    public void run() {
        var fileTree = new FileTree(path);
        if (fileTree.exists()) {
            fileTree.delete();
            LOGGER.log(System.Logger.Level.INFO, "Removed {0}", path);
        } else {
            LOGGER.log(System.Logger.Level.WARNING, "{0} does not exist", path);
        }
    }
}
