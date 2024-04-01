package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.DefinitionTranslator;
import com.github.maximtereshchenko.conveyor.api.port.ManualDefinition;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

final class LocalDirectoryRepository implements Repository {

    private final Path path;
    private final DefinitionTranslator definitionTranslator;

    LocalDirectoryRepository(Path path, DefinitionTranslator definitionTranslator) {
        this.path = path;
        this.definitionTranslator = definitionTranslator;
    }

    @Override
    public Optional<ManualDefinition> manualDefinition(
        String group,
        String name,
        SemanticVersion semanticVersion
    ) {
        return existing(fullPath(group, name, semanticVersion, Extension.JSON))
            .map(definitionTranslator::manualDefinition);
    }

    @Override
    public Optional<Path> path(
        String group,
        String name,
        SemanticVersion semanticVersion
    ) {
        return existing(fullPath(group, name, semanticVersion, Extension.JAR));
    }

    Path storedJar(String group, String name, SemanticVersion semanticVersion, byte[] bytes)
        throws IOException {
        return Files.write(fullPath(group, name, semanticVersion, Extension.JAR), bytes);
    }

    ManualDefinition stored(
        String group,
        String name,
        SemanticVersion semanticVersion,
        ManualDefinition manualDefinition
    ) {
        definitionTranslator.write(
            manualDefinition,
            fullPath(group, name, semanticVersion, Extension.JSON)
        );
        return manualDefinition;
    }

    private Optional<Path> existing(Path path) {
        if (Files.exists(path)) {
            return Optional.of(path);
        }
        return Optional.empty();
    }

    private Path fullPath(String group, String name, SemanticVersion version, Extension extension) {
        try {
            return Files.createDirectories(path)
                .resolve(
                    "%s:%s-%s.%s".formatted(
                        group,
                        name,
                        version,
                        extension.name().toLowerCase(Locale.ROOT)
                    )
                )
                .normalize();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private enum Extension {JSON, JAR}
}
