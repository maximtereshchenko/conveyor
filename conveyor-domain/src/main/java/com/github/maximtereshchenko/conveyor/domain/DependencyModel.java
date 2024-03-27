package com.github.maximtereshchenko.conveyor.domain;

sealed interface DependencyModel permits ArtifactDependencyModel, SchematicDependencyModel {

    String group();

    String name();

    DependencyModel override(DependencyModel base);
}
