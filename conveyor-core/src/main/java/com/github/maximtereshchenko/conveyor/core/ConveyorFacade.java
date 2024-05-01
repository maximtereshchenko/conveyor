package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public final class ConveyorFacade implements ConveyorModule {

    private static final System.Logger LOGGER = System.getLogger(ConveyorFacade.class.getName());

    private final SchematicModelFactory schematicModelFactory;
    private final ClassPathFactory classPathFactory;
    private final PomDefinitionFactory pomDefinitionFactory;
    private final SchematicDefinitionConverter schematicDefinitionConverter;
    private final PreferencesFactory preferencesFactory;

    public ConveyorFacade(SchematicDefinitionConverter schematicDefinitionConverter) {
        this.schematicDefinitionConverter =
            new CachingSchematicDefinitionConverter(schematicDefinitionConverter);
        this.schematicModelFactory = new SchematicModelFactory(this.schematicDefinitionConverter);
        this.classPathFactory = new ClassPathFactory();
        this.pomDefinitionFactory = PomDefinitionFactory.configured();
        this.preferencesFactory = new PreferencesFactory(this.schematicModelFactory);
    }

    @Override
    public void construct(Path path, Stage stage) {
        var start = Instant.now();
        schematics(path).construct(stage);
        LOGGER.log(
            System.Logger.Level.INFO,
            () -> "Construction took %ds".formatted(
                Duration.between(start, Instant.now()).getSeconds()
            )
        );
    }

    private Schematics schematics(Path path) {
        var schematics = schematicModelFactory.extendableLocalInheritanceHierarchyModels(path)
            .stream()
            .map(extendableLocalInheritanceHierarchyModel ->
                new Schematic(
                    extendableLocalInheritanceHierarchyModel,
                    classPathFactory,
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
