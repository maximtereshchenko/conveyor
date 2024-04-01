package com.github.maximtereshchenko.conveyor.domain;

record SchematicTemplateModel(String group, String name, SemanticVersion version)
    implements TemplateModel {}
