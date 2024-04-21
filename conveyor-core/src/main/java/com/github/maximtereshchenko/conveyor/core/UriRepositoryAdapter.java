package com.github.maximtereshchenko.conveyor.core;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

final class UriRepositoryAdapter implements Repository {

    private final UriRepository<Path> original;

    UriRepositoryAdapter(UriRepository<Path> original) {
        this.original = original;
    }

    @Override
    public Optional<Path> path(Id id, SemanticVersion semanticVersion, Classifier classifier) {
        return original.artifact(
            URI.create(
                "%s/%s/%s/%s-%s.%s".formatted(
                    id.group().replace('.', '/'),
                    id.name(),
                    semanticVersion,
                    id.name(),
                    semanticVersion,
                    switch (classifier) {
                        case SCHEMATIC_DEFINITION -> "json";
                        case MODULE -> "jar";
                    }
                )
            )
        );
    }
}
