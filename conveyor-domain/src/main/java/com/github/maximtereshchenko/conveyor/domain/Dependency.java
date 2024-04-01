package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

interface Dependency extends Artifact {

    DependencyScope scope();
}
