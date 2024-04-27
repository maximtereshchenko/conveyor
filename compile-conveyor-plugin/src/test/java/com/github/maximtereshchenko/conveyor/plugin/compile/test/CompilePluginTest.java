package com.github.maximtereshchenko.conveyor.plugin.compile.test;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.compile.CompilePlugin;
import com.github.maximtereshchenko.test.common.Directories;
import com.github.maximtereshchenko.test.common.JimfsExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ExtendWith(JimfsExtension.class)
abstract class CompilePluginTest {

    ConveyorPlugin plugin = new CompilePlugin();

    Path srcMainJava(Path path) {
        return path.resolve("src").resolve("main").resolve("java");
    }

    Path explodedModule(Path path) {
        return path.resolve("exploded-module");
    }

    Path moduleInfoJava(Path path) {
        return path.resolve("module-info.java");
    }

    Path moduleInfoClass(Path path) {
        return path.resolve("module-info.class");
    }

    void write(Path path, String content) throws IOException {
        Files.writeString(Directories.createDirectoriesForFile(path), content);
    }
}
