package com.github.maximtereshchenko.conveyor.api.port;

import java.nio.file.Path;
import java.util.Collection;

public record ProjectDefinition(String name, int version, Path repository, Collection<PluginDefinition> plugins) {}
