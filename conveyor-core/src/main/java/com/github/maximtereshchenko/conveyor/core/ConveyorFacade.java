package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public final class ConveyorFacade implements ConveyorModule {

    private final SchematicModelFactory modelFactory;
    private final ModulePathFactory modulePathFactory;
    private final PomDefinitionFactory pomDefinitionFactory;
    private final SchematicDefinitionConverter schematicDefinitionConverter;

    public ConveyorFacade(SchematicDefinitionConverter schematicDefinitionConverter) {
        this.modelFactory = new SchematicModelFactory(schematicDefinitionConverter);
        this.modulePathFactory = new ModulePathFactory();
        this.pomDefinitionFactory = PomDefinitionFactory.configured();
        this.schematicDefinitionConverter = schematicDefinitionConverter;
    }

    @Override
    public void construct(Path path, Stage stage) {
        schematics(path).construct(stage);
    }

    private Schematics schematics(Path path) {
        var schematics = modelFactory.extendableLocalInheritanceHierarchyModels(path)
            .stream()
            .map(extendableLocalInheritanceHierarchyModel ->
                new Schematic(
                    extendableLocalInheritanceHierarchyModel,
                    modulePathFactory,
                    pomDefinitionFactory,
                    schematicDefinitionConverter,
                    modelFactory
                )
            )
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return new Schematics(schematics, initial(schematics, path));
    }

    private Schematic initial(LinkedHashSet<Schematic> schematics, Path path) {
        return schematics.stream()
            .filter(schematic -> schematic.locatedAt(path))
            .findAny()
            .orElseThrow();
    }
}
