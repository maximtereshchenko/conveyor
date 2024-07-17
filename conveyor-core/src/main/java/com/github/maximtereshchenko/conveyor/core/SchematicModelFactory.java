package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.LinkedHashSet;

interface SchematicModelFactory {

    LinkedHashSet<ExtendableLocalInheritanceHierarchyModel> extendableLocalInheritanceHierarchyModels(
        Path path
    );

    CompleteInheritanceHierarchyModel completeInheritanceHierarchyModel(
        ExtendableLocalInheritanceHierarchyModel localModel,
        Repositories repositories
    );

    InheritanceHierarchyModel<SchematicModel> inheritanceHierarchyModel(
        Id id,
        Version version,
        Repositories repositories
    );
}
