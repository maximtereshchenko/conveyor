package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.api.port.ArtifactDependencyDefinition;
import com.github.maximtereshchenko.conveyor.api.port.DefinitionReader;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinition;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDependencyDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;

import java.nio.file.Path;

final class SchematicArtifact implements Artifact {

    private final SchematicDependencyDefinition schematicDependencyDefinition;
    private final SchematicProducts schematicProducts;
    private final DefinitionReader definitionReader;
    private final Repository repository;

    SchematicArtifact(
        SchematicDependencyDefinition schematicDependencyDefinition,
        SchematicProducts schematicProducts,
        DefinitionReader definitionReader,
        Repository repository
    ) {
        this.schematicDependencyDefinition = schematicDependencyDefinition;
        this.schematicProducts = schematicProducts;
        this.definitionReader = definitionReader;
        this.repository = repository;
    }

    @Override
    public String name() {
        return schematicDependencyDefinition.schematic();
    }

    @Override
    public int version() {
        return schematicDefinition().version();
    }

    @Override
    public ImmutableSet<Artifact> dependencies() {
        return schematicDefinition()
            .dependencies()
            .stream()
            .filter(definition -> definition.scope() != DependencyScope.TEST)
            .map(definition ->
                switch (definition) {
                    case ArtifactDependencyDefinition artifact ->
                        new PackagedArtifact(artifact.name(), artifact.version(), repository);
                    case SchematicDependencyDefinition schematic ->
                        new SchematicArtifact(schematic, schematicProducts, definitionReader, repository);
                }
            )
            .collect(new ImmutableSetCollector<>());
    }

    @Override
    public Path modulePath() {
        return product(ProductType.MODULE);
    }

    private SchematicDefinition schematicDefinition() {
        return definitionReader.schematicDefinition(product(ProductType.SCHEMATIC_DEFINITION));
    }

    private Path product(ProductType schematicDefinition) {
        return schematicProducts.byType(schematicDependencyDefinition.schematic(), schematicDefinition)
            .iterator()
            .next();
    }
}
