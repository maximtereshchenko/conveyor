package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

final class PartialSchematicHierarchy
    extends SchematicHierarchy<OtherManualTemplateModel, SchematicTemplateModel> {

    private PartialSchematicHierarchy(
        LinkedHashSet<SchematicModel<? extends SchematicTemplateModel>> models
    ) {
        super(models);
    }

    PartialSchematicHierarchy(SchematicModel<SchematicTemplateModel> model) {
        super(new LinkedHashSet<>(Set.of(model)));
    }

    @Override
    public OtherManualTemplateModel template() {
        return switch (models().getFirst().template()) {
            case OtherManualTemplateModel model -> model;
            case ManualTemplateModel ignored -> throw new IllegalArgumentException();
            case OtherSchematicTemplateModel ignored -> throw new IllegalArgumentException();
        };
    }

    PartialSchematicHierarchy inheritedFrom(StandaloneSchematicModel standaloneSchematicModel) {
        var copy = new LinkedHashSet<>(models());
        copy.addFirst(standaloneSchematicModel);
        return new PartialSchematicHierarchy(copy);
    }

    Path rootPath() {
        return models().getFirst().path();
    }
}
