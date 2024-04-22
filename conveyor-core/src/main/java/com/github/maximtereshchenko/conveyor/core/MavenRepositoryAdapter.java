package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.api.schematic.*;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

final class MavenRepositoryAdapter implements Repository<InputStream> {

    private final Repository<Path> original;
    private final PomModelFactory pomModelFactory;
    private final SchematicDefinitionConverter schematicDefinitionConverter;

    MavenRepositoryAdapter(
        Repository<Path> original,
        PomModelFactory pomModelFactory,
        SchematicDefinitionConverter schematicDefinitionConverter
    ) {
        this.original = original;
        this.pomModelFactory = pomModelFactory;
        this.schematicDefinitionConverter = schematicDefinitionConverter;
    }

    @Override
    public Optional<InputStream> artifact(
        Id id,
        SemanticVersion semanticVersion,
        Classifier classifier
    ) {
        return switch (classifier) {
            case SCHEMATIC_DEFINITION -> original.artifact(id, semanticVersion, Classifier.POM)
                .map(this::pomModel)
                .map(this::schematicDefinition)
                .map(this::inputStream);
            case MODULE, POM -> original.artifact(id, semanticVersion, classifier)
                .map(this::inputStream);
        };
    }

    private InputStream inputStream(Path path) {
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private InputStream inputStream(SchematicDefinition schematicDefinition) {
        return new ByteArrayInputStream(schematicDefinitionConverter.bytes(schematicDefinition));
    }

    private PomModel pomModel(Path path) {
        try (var inputStream = Files.newInputStream(path)) {
            return pomModelFactory.pomModel(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private SchematicDefinition schematicDefinition(PomModel pomModel) {
        return new SchematicDefinition(
            either(pomModel, PomModel::groupId, PomModel.Parent::groupId),
            pomModel.artifactId(),
            either(pomModel, PomModel::version, PomModel.Parent::version),
            template(pomModel),
            List.of(),
            List.of(),
            pomModel.properties(),
            preferences(pomModel),
            List.of(),
            dependencies(pomModel)
        );
    }

    private List<DependencyDefinition> dependencies(PomModel pomModel) {
        return pomModel.dependencies()
            .stream()
            .map(reference ->
                new DependencyDefinition(
                    reference.groupId(),
                    reference.artifactId(),
                    Optional.of(reference.version()),
                    reference.scope()
                        .map(scope ->
                            switch (scope) {
                                case COMPILE, RUNTIME, SYSTEM, PROVIDED ->
                                    DependencyScope.IMPLEMENTATION;
                                case TEST -> DependencyScope.TEST;
                            }
                        )
                )
            )
            .toList();
    }

    private PreferencesDefinition preferences(PomModel pomModel) {
        return new PreferencesDefinition(
            List.of(),
            pomModel.dependencyManagement()
                .stream()
                .map(reference ->
                    new ArtifactPreferenceDefinition(
                        reference.groupId(),
                        reference.artifactId(),
                        reference.version()
                    )
                )
                .toList()
        );
    }

    private TemplateDefinition template(PomModel pomModel) {
        return pomModel.parent()
            .<TemplateDefinition>map(parent ->
                new SchematicTemplateDefinition(
                    parent.groupId(),
                    parent.artifactId(),
                    parent.version()
                )
            )
            .orElseGet(NoTemplateDefinition::new);
    }

    private String either(
        PomModel pomModel,
        Function<PomModel, Optional<String>> pomFunction,
        Function<PomModel.Parent, String> parentFunction
    ) {
        return pomFunction.apply(pomModel)
            .or(() -> pomModel.parent().map(parentFunction))
            .orElseThrow();
    }
}
