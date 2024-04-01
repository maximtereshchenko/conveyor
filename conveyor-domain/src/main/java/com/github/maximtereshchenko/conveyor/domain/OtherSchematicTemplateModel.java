package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;

record OtherSchematicTemplateModel(Path path) implements SchematicTemplateModel {}
