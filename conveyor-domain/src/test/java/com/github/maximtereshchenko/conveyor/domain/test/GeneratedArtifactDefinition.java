package com.github.maximtereshchenko.conveyor.domain.test;

import java.nio.file.Path;

record GeneratedArtifactDefinition(String moduleName, String className, String name, int version, Path jar) {}
