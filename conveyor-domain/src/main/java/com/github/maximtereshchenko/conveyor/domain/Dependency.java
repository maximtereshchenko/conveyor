package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

interface Dependency {

    String name();

    boolean in(ImmutableSet<DependencyScope> scopes);

    Artifact artifact(Repository repository);
}
