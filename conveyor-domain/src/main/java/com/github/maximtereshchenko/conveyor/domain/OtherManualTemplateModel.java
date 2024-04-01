package com.github.maximtereshchenko.conveyor.domain;

record OtherManualTemplateModel(
    String name, SemanticVersion version
) implements SchematicTemplateModel, ManualTemplateModel {}
