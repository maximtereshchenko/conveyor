package com.github.maximtereshchenko.conveyor.domain;

import java.util.LinkedHashSet;
import java.util.List;

final class FullSchematicHierarchy extends SchematicHierarchy<NoTemplateModel, TemplateModel> {

    FullSchematicHierarchy(
        ManualHierarchy manualHierarchy,
        PartialSchematicHierarchy partialSchematicHierarchy
    ) {
        super(
            new LinkedHashSet<>(
                List.of(
                    new ManualHierarchyAdapter(manualHierarchy),
                    partialSchematicHierarchy
                )
            )
        );
    }

    @Override
    public NoTemplateModel template() {
        return new NoTemplateModel();
    }
}
