package com.github.maximtereshchenko.conveyor.core;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.maximtereshchenko.conveyor.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.jackson.JacksonAdapter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

final class BuilderFactory {

    private final JacksonAdapter jacksonAdapter;
    private final XmlMapper xmlMapper;
    private final Compiler compiler;

    BuilderFactory(
        JacksonAdapter jacksonAdapter,
        XmlMapper xmlMapper,
        Compiler compiler
    ) {
        this.jacksonAdapter = jacksonAdapter;
        this.xmlMapper = xmlMapper;
        this.compiler = compiler;
    }

    RepositoryBuilder repositoryBuilder(Path path) throws IOException {
        return new RepositoryBuilder(Files.createTempDirectory(path, null));
    }

    PomBuilder pomBuilder() {
        return new PomBuilder(xmlMapper);
    }

    JarBuilder jarBuilder(String template, Path path) throws IOException, URISyntaxException {
        return JarBuilder.from(template, Files.createTempDirectory(path, null), compiler);
    }

    SchematicDefinitionBuilder schematicDefinitionBuilder() {
        return new SchematicDefinitionBuilder(jacksonAdapter);
    }
}
