package com.github.maximtereshchenko.conveyor.domain;

sealed interface DependencyModel permits ArtifactDependencyModel, SchematicDependencyModel {

    String name();

    DependencyModel override(DependencyModel base);
}
