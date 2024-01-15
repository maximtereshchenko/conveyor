package com.github.maximtereshchenko.conveyor.plugin.api;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import java.nio.file.Path;
import java.util.Set;

public interface Project {

    Path projectDirectory();

    Set<Path> modulePath(DependencyScope... scopes);
}
