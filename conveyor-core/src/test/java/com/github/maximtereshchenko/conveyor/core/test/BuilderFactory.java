package com.github.maximtereshchenko.conveyor.core.test;

import com.fasterxml.jackson.dataformat.xml.shadowed.XmlMapper;
import com.github.maximtereshchenko.conveyor.jackson.JacksonAdapter;

final class BuilderFactory {

    private final JacksonAdapter jacksonAdapter;
    private final XmlMapper xmlMapper;

    BuilderFactory(JacksonAdapter jacksonAdapter, XmlMapper xmlMapper) {
        this.jacksonAdapter = jacksonAdapter;
        this.xmlMapper = xmlMapper;
    }

    RepositoryBuilder repositoryBuilder() {
        return new RepositoryBuilder();
    }

    PomBuilder pomBuilder() {
        return new PomBuilder(xmlMapper);
    }

    JarBuilder jarBuilder(String templateDirectory) {
        return JarBuilder.from(templateDirectory);
    }

    SchematicDefinitionBuilder schematicDefinitionBuilder() {
        return new SchematicDefinitionBuilder(jacksonAdapter);
    }
}
