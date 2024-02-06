package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDependencyDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;

import java.nio.file.Path;
import java.util.List;

final class SchematicProductsDependency implements Dependency {

    private final SchematicDependencyDefinition schematicDependencyDefinition;
    private final SchematicProducts schematicProducts;

    SchematicProductsDependency(
        SchematicDependencyDefinition schematicDependencyDefinition,
        SchematicProducts schematicProducts
    ) {
        this.schematicDependencyDefinition = schematicDependencyDefinition;
        this.schematicProducts = schematicProducts;
    }

    @Override
    public String name() {
        return schematicDependencyDefinition.schematic();
    }

    @Override
    public int version() {
        return 0; //TODO
    }

    @Override
    public Dependencies dependencies() {
        return new Dependencies(); //TODO
    }

    @Override
    public Path modulePath() {
        return schematicProducts.byType(schematicDependencyDefinition.schematic(), ProductType.MODULE)
            .iterator()
            .next();
    }

    @Override
    public boolean hasAny(DependencyScope... scopes) {
        return List.of(scopes).contains(schematicDependencyDefinition.scope());
    }
}
