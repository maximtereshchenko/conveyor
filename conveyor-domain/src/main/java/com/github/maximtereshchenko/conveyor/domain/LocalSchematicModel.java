package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

interface LocalSchematicModel extends SchematicModel {

    Path path();

    Path templatePath();

    LinkedHashSet<Path> inclusions();

    Set<RepositoryModel> repositories();
}
