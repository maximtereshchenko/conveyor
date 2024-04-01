package com.github.maximtereshchenko.conveyor.domain;

sealed interface SchematicTemplateModel extends TemplateModel
    permits OtherManualTemplateModel, OtherSchematicTemplateModel, NoTemplateModel {}
