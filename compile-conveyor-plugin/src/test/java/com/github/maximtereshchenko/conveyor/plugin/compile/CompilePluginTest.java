package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.test.Directories;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

abstract class CompilePluginTest {

    ConveyorPlugin plugin = new CompilePlugin();

    Path srcMainJava(Path path) {
        return path.resolve("src").resolve("main").resolve("java");
    }

    Path classes(Path path) {
        return path.resolve("classes");
    }

    Path srcTestJava(Path path) {
        return path.resolve("src").resolve("test").resolve("java");
    }

    Path testClasses(Path path) {
        return path.resolve("test-classes");
    }

    Map<String, String> configuration(Path path) {
        return Map.of(
            "sources.directory", srcMainJava(path).toString(),
            "classes.directory", classes(path).toString(),
            "test.sources.directory", srcTestJava(path).toString(),
            "test.classes.directory", testClasses(path).toString()
        );
    }

    void write(Path path, String content) throws IOException {
        Files.writeString(Directories.createDirectoriesForFile(path), content);
    }
}
