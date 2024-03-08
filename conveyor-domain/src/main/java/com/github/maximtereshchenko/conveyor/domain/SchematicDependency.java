package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.api.port.DefinitionReader;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDependencyDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

final class SchematicDependency implements Dependency {

    private final SchematicDependencyDefinition schematicDependencyDefinition;
    private final SchematicProducts schematicProducts;
    private final DefinitionReader definitionReader;

    SchematicDependency(
        SchematicDependencyDefinition schematicDependencyDefinition,
        SchematicProducts schematicProducts,
        DefinitionReader definitionReader
    ) {
        this.schematicDependencyDefinition = schematicDependencyDefinition;
        this.schematicProducts = schematicProducts;
        this.definitionReader = definitionReader;
    }

    @Override
    public String name() {
        return schematicDependencyDefinition.schematic();
    }

    @Override
    public boolean in(ImmutableSet<DependencyScope> scopes) {
        return scopes.contains(schematicDependencyDefinition.scope());
    }

    @Override
    public Artifact artifact(Repositories repositories) {
        return new SchematicArtifact(
            schematicDependencyDefinition,
            schematicProducts,
            definitionReader,
            repositories
        );
    }
}
