package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.LinkedHashSet;

interface LocalSchematicModel extends SchematicModel {

    Path path();

    Path templatePath();

    LinkedHashSet<Path> inclusions();

    LinkedHashSet<RepositoryModel> repositories();
}
