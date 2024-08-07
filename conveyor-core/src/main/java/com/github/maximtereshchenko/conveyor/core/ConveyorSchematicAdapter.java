package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.schematic.DependencyScope;
import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;
import com.github.maximtereshchenko.conveyor.plugin.api.ClasspathScope;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

final class ConveyorSchematicAdapter implements ConveyorSchematic {

    private final Path path;
    private final Id id;
    private final Version version;
    private final Properties properties;
    private final Dependencies dependencies;
    private final Repositories repositories;

    ConveyorSchematicAdapter(
        Path path, Id id,
        Version version,
        Properties properties,
        Dependencies dependencies,
        Repositories repositories
    ) {
        this.path = path;
        this.id = id;
        this.version = version;
        this.properties = properties;
        this.dependencies = dependencies;
        this.repositories = repositories;
    }

    @Override
    public String group() {
        return id.group();
    }

    @Override
    public String name() {
        return id.name();
    }

    @Override
    public String version() {
        return version.toString();
    }

    @Override
    public Path path() {
        return path;
    }

    @Override
    public Optional<String> propertyValue(String key) {
        return properties.value(key);
    }

    @Override
    public Set<Path> classpath(Set<ClasspathScope> scopes) {
        return dependencies.classpath(
            scopes.stream()
                .map(ClasspathScope::toString)
                .map(DependencyScope::valueOf)
                .collect(Collectors.toSet())
        );
    }

    @Override
    public void publish(String repository, Path path, ArtifactClassifier artifactClassifier) {
        repositories.publish(
            repository,
            id,
            version,
            switch (artifactClassifier) {
                case SCHEMATIC_DEFINITION -> Repository.Classifier.SCHEMATIC_DEFINITION;
                case CLASSES -> Repository.Classifier.CLASSES;
            },
            path
        );
    }
}
