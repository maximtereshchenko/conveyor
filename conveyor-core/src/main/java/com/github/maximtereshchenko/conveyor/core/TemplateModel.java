package com.github.maximtereshchenko.conveyor.core;

sealed interface TemplateModel permits NoTemplateModel, SchematicTemplateModel {}
