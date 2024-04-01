package com.github.maximtereshchenko.conveyor.common.api;

import java.nio.file.Path;

public record Product(SchematicCoordinates schematicCoordinates, Path path, ProductType type) {}
