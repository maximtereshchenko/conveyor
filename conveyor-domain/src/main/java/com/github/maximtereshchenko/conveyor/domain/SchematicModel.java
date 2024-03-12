package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

interface SchematicModel<T extends TemplateModel> extends Model<T, DependencyModel> {

    Path path();

    Set<RepositoryModel> repositories();

    LinkedHashSet<Path> inclusions();
}
