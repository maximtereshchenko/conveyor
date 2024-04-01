package com.github.maximtereshchenko.conveyor.common.api;

import java.nio.file.Path;

public record BuildFile(Path path, BuildFileType type) {}
