package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.test.Directories;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

abstract class BaseTest {

    ConveyorPlugin plugin = new CompilePlugin();

    void write(Path path, String content) throws IOException {
        Files.writeString(Directories.createDirectoriesForFile(path), content);
    }
}
