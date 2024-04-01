package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;

record Id(String group, String name) {

    SchematicCoordinates coordinates(SemanticVersion semanticVersion) {
        return new SchematicCoordinates(group, name, semanticVersion.toString());
    }
}
