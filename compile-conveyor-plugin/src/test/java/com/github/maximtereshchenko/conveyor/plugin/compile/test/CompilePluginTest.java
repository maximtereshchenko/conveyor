package com.github.maximtereshchenko.conveyor.plugin.compile.test;

import com.github.maximtereshchenko.conveyor.common.test.Directories;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.compile.CompilePlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

abstract class CompilePluginTest {

    ConveyorPlugin plugin = new CompilePlugin();

    Path srcMainJava(Path path) {
        return path.resolve("src").resolve("main").resolve("java");
    }

    Path explodedJar(Path path) {
        return path.resolve("exploded-jar");
    }

    void write(Path path, String content) throws IOException {
        Files.writeString(Directories.createDirectoriesForFile(path), content);
    }
}
