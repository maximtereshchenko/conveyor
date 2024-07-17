package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.Set;

interface ClasspathFactory {

    Set<Path> classpath(Set<? extends Artifact> artifacts);
}
