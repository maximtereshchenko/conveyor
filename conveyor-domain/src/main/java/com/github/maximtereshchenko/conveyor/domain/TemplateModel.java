package com.github.maximtereshchenko.conveyor.domain;

sealed interface TemplateModel permits NoTemplateModel, SchematicTemplateModel {}
