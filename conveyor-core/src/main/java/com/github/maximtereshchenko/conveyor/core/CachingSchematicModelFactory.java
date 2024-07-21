package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class CachingSchematicModelFactory implements SchematicModelFactory {

    private final SchematicModelFactory original;
    private final Map<Key, InheritanceHierarchyModel<SchematicModel>> cache =
        new ConcurrentHashMap<>();

    CachingSchematicModelFactory(SchematicModelFactory original) {
        this.original = original;
    }

    @Override
    public LinkedHashSet<ExtendableLocalInheritanceHierarchyModel> extendableLocalInheritanceHierarchyModels(
        Path path
    ) {
        return original.extendableLocalInheritanceHierarchyModels(path);
    }

    @Override
    public CompleteInheritanceHierarchyModel completeInheritanceHierarchyModel(
        ExtendableLocalInheritanceHierarchyModel localModel,
        Repositories repositories
    ) {
        return original.completeInheritanceHierarchyModel(localModel, repositories);
    }

    @Override
    public InheritanceHierarchyModel<SchematicModel> inheritanceHierarchyModel(
        Id id,
        Version version,
        Repositories repositories
    ) {
        return cache.computeIfAbsent(
            new Key(id, version),
            key -> original.inheritanceHierarchyModel(id, version, repositories)
        );
    }

    private record Key(Id id, Version version) {}
}
