package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.jackson.JacksonAdapter;
import com.github.maximtereshchenko.conveyor.jackson.dataformat.xml.XmlMapper;

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
