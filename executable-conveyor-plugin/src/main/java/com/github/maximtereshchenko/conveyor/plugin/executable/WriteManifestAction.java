package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.files.FileTree;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

final class WriteManifestAction implements Supplier<Optional<Path>> {

    private static final System.Logger LOGGER =
        System.getLogger(WriteManifestAction.class.getName());

    private final Path classesDirectory;
    private final String mainClass;

    WriteManifestAction(Path classesDirectory, String mainClass) {
        this.classesDirectory = classesDirectory;
        this.mainClass = mainClass;
    }

    @Override
    public Optional<Path> get() {
        if (Files.exists(classesDirectory)) {
            write();
        }
        return Optional.empty();
    }

    private void write() {
        var manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        var destination = classesDirectory.resolve("META-INF").resolve("MANIFEST.MF");
        new FileTree(destination).write(manifest::write);
        LOGGER.log(System.Logger.Level.INFO, "Wrote {0}", destination);
    }
}
