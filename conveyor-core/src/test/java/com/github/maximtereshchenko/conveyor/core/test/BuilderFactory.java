package com.github.maximtereshchenko.conveyor.core.test;

import com.fasterxml.jackson.dataformat.xml.shadowed.XmlMapper;
import com.github.maximtereshchenko.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.jackson.JacksonAdapter;
import com.github.maximtereshchenko.test.common.Directories;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

final class BuilderFactory {

    private final JacksonAdapter jacksonAdapter;
    private final XmlMapper xmlMapper;
    private final Compiler compiler;
    private final Path path;

    BuilderFactory(
        JacksonAdapter jacksonAdapter,
        XmlMapper xmlMapper,
        Compiler compiler,
        Path path
    ) {
        this.jacksonAdapter = jacksonAdapter;
        this.xmlMapper = xmlMapper;
        this.compiler = compiler;
        this.path = path;
    }

    RepositoryBuilder repositoryBuilder() throws IOException {
        return new RepositoryBuilder(Directories.temporaryDirectory(path));
    }

    PomBuilder pomBuilder() {
        return new PomBuilder(xmlMapper);
    }

    JarBuilder jarBuilder(String templateDirectory) throws IOException, URISyntaxException {
        return JarBuilder.from(templateDirectory, Directories.temporaryDirectory(path), compiler);
    }

    SchematicDefinitionBuilder schematicDefinitionBuilder() {
        return new SchematicDefinitionBuilder(jacksonAdapter);
    }
}
