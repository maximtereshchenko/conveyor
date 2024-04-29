package com.github.maximtereshchenko.conveyor.plugin.test;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class FakeConveyorSchematicBuilder {

    private final Path discoveryDirectory;
    private final Set<Path> dependencies = new HashSet<>();
    private Path constructionDirectory;

    private FakeConveyorSchematicBuilder(Path discoveryDirectory) {
        this.discoveryDirectory = discoveryDirectory;
    }

    public static FakeConveyorSchematicBuilder discoveryDirectory(Path discoveryDirectory) {
        return new FakeConveyorSchematicBuilder(discoveryDirectory)
            .constructionDirectory(discoveryDirectory.resolve(".conveyor"));
    }

    public FakeConveyorSchematicBuilder constructionDirectory(Path constructionDirectory) {
        this.constructionDirectory = constructionDirectory;
        return this;
    }

    public FakeConveyorSchematicBuilder dependency(Path dependency) {
        dependencies.add(dependency);
        return this;
    }

    public FakeConveyorSchematic build() {
        return new FakeConveyorSchematic(discoveryDirectory, constructionDirectory, dependencies);
    }
}
