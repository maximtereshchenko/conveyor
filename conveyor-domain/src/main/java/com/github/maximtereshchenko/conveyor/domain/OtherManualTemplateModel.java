package com.github.maximtereshchenko.conveyor.domain;

record OtherManualTemplateModel(String group, String name, SemanticVersion version)
    implements SchematicTemplateModel, ManualTemplateModel {}
