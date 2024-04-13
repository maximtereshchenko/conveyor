package com.github.maximtereshchenko.conveyor.plugin.compile.test;

import com.github.maximtereshchenko.jimfs.JimfsExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ExtendWith(JimfsExtension.class)
abstract class CompilePluginTest {

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
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
