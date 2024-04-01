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
    public Optional<Repository> repository() {
        return Optional.empty();
    }

    @Override
    public Properties properties(Repository repository) {
        var manualDefinition = manualDefinition(repository);
        return properties(manualDefinition.properties())
            .override(template(manualDefinition.template()).properties(repository));
    }

    @Override
    public Plugins plugins(Repository repository) {
        var manualDefinition = manualDefinition(repository);
        return plugins(manualDefinition.plugins())
            .override(template(manualDefinition.template()).plugins(repository));
    }

    @Override
    public Dependencies dependencies(Repository repository, SchematicProducts schematicProducts) {
        var manualDefinition = manualDefinition(repository);
        return dependencies(manualDefinition.dependencies(), schematicProducts)
            .override(template(manualDefinition.template()).dependencies(repository, schematicProducts));
    }

    @Override
    public Optional<Schematic> root() {
        return Optional.empty();
    }

    @Override
    public boolean inheritsFrom(Schematic schematic) {
        return false;
    }

    private ManualDefinition manualDefinition(Repository repository) {
        return repository.manualDefinition(name, version);
    }

    private Template template(TemplateForManualDefinition templateDefinition) {
        return switch (templateDefinition) {
            case ManualTemplateDefinition definition ->
                new Manual(definitionReader(), definition.name(), definition.version());
            case NoExplicitlyDefinedTemplate ignored -> new EmptyTemplate();
        };
    }
}
