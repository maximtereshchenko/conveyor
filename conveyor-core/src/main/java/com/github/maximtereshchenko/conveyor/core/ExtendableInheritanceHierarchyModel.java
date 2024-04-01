package com.github.maximtereshchenko.conveyor.core;

interface ExtendableInheritanceHierarchyModel
    <T extends SchematicModel, M extends ExtendableInheritanceHierarchyModel<T, M>> {

    M inheritedFrom(T model);

    boolean inheritsFrom(M extendableInheritanceHierarchyModel);
}
