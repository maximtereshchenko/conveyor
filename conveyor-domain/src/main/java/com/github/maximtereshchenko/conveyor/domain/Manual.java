package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.api.port.*;

import java.util.Optional;

final class Manual extends Definition {

    private final String name;
    private final int version;

    Manual(DefinitionReader definitionReader, String name, int version) {
        super(definitionReader);
        this.name = name;
        this.version = version;
    }

    static Template superManual(DefinitionReader definitionReader) {
        return new Manual(definitionReader, "super-manual", 1);
    }

    @Override
    public Repositories repositories() {
        return new Repositories();
    }

    @Override
    public Properties properties(Repositories repositories) {
        var manualDefinition = manualDefinition(repositories);
        return properties(manualDefinition.properties())
            .override(template(manualDefinition.template()).properties(repositories));
    }

    @Override
    public Plugins plugins(Repositories repositories) {
        var manualDefinition = manualDefinition(repositories);
        return plugins(manualDefinition.plugins())
            .override(template(manualDefinition.template()).plugins(repositories));
    }

    @Override
    public Dependencies dependencies(Repositories repositories, SchematicProducts schematicProducts) {
        var manualDefinition = manualDefinition(repositories);
        return dependencies(manualDefinition.dependencies(), schematicProducts)
            .override(template(manualDefinition.template()).dependencies(repositories, schematicProducts));
    }

    @Override
    public Optional<Schematic> root() {
        return Optional.empty();
    }

    @Override
    public boolean inheritsFrom(Schematic schematic) {
        return false;
    }

    private ManualDefinition manualDefinition(Repositories repositories) {
        return repositories.manualDefinition(name, version);
    }

    private Template template(TemplateForManualDefinition templateDefinition) {
        return switch (templateDefinition) {
            case ManualTemplateDefinition definition ->
                new Manual(definitionReader(), definition.name(), definition.version());
            case NoExplicitlyDefinedTemplate ignored -> new EmptyTemplate();
        };
    }
}
