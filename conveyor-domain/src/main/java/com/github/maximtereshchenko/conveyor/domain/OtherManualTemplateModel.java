package com.github.maximtereshchenko.conveyor.domain;

record OtherManualTemplateModel(String name, int version) implements SchematicTemplateModel, ManualTemplateModel {}
