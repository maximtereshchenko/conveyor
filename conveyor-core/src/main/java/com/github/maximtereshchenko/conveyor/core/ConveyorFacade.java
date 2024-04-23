package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public final class ConveyorFacade implements ConveyorModule {

    private final SchematicModelFactory schematicModelFactory;
    private final ModulePathFactory modulePathFactory;
    private final PomDefinitionFactory pomDefinitionFactory;
    private final SchematicDefinitionConverter schematicDefinitionConverter;
    private final PreferencesFactory preferencesFactory;

    public ConveyorFacade(SchematicDefinitionConverter schematicDefinitionConverter) {
        this.schematicModelFactory = new SchematicModelFactory(schematicDefinitionConverter);
        this.modulePathFactory = new ModulePathFactory();
        this.pomDefinitionFactory = PomDefinitionFactory.configured();
        this.schematicDefinitionConverter = schematicDefinitionConverter;
        this.preferencesFactory = new PreferencesFactory(this.schematicModelFactory);
    }

    @Override
    public void construct(Path path, Stage stage) {
        schematics(path).construct(stage);
    }

    private Schematics schematics(Path path) {
        var schematics = schematicModelFactory.extendableLocalInheritanceHierarchyModels(path)
            .stream()
            .map(extendableLocalInheritanceHierarchyModel ->
                new Schematic(
                    extendableLocalInheritanceHierarchyModel,
                    modulePathFactory,
                    pomDefinitionFactory,
                    schematicDefinitionConverter,
                    schematicModelFactory,
                    preferencesFactory
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
