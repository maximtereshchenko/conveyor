package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.Stage;
import com.github.maximtereshchenko.conveyor.api.TracingOutputLevel;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.api.port.TracingOutput;

import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public final class ConveyorFacade implements ConveyorModule {

    private final SchematicModelFactory schematicModelFactory;
    private final ClasspathFactory classpathFactory;
    private final PomDefinitionFactory pomDefinitionFactory;
    private final SchematicDefinitionConverter schematicDefinitionConverter;
    private final PreferencesFactory preferencesFactory;
    private final Tracer tracer;

    public ConveyorFacade(
        SchematicDefinitionConverter schematicDefinitionConverter,
        TracingOutput tracingOutput,
        TracingOutputLevel tracingOutputLevel
    ) {
        this.tracer = new Tracer(tracingOutput, tracingOutputLevel);
        this.schematicDefinitionConverter =
            new CachingSchematicDefinitionConverter(
                new TracingSchematicDefinitionConverter(schematicDefinitionConverter, tracer)
            );
        this.schematicModelFactory = new CachingSchematicModelFactory(
            new DefaultSchematicModelFactory(
                this.schematicDefinitionConverter,
                this.tracer
            )
        );
        this.classpathFactory = new CachingClasspathFactory(new DefaultClasspathFactory(tracer));
        this.pomDefinitionFactory = PomDefinitionFactory.configured();
        this.preferencesFactory = new PreferencesFactory(this.schematicModelFactory);
    }

    @Override
    public void construct(Path path, List<Stage> stages) {
        var start = Instant.now();
        schematics(path).construct(stages);
        tracer.submitConstructionDuration(start, Instant.now());
    }

    private Schematics schematics(Path path) {
        var schematics = schematicModelFactory.extendableLocalInheritanceHierarchyModels(path)
            .stream()
            .map(extendableLocalInheritanceHierarchyModel ->
                new Schematic(
                    extendableLocalInheritanceHierarchyModel,
                    classpathFactory,
                    pomDefinitionFactory,
                    schematicDefinitionConverter,
                    schematicModelFactory,
                    preferencesFactory,
                    tracer.withContext(
                        "schematic",
                        extendableLocalInheritanceHierarchyModel.id()
                    )
                )
            )
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return new Schematics(schematics, initial(schematics, path), tracer);
    }

    private Schematic initial(LinkedHashSet<Schematic> schematics, Path path) {
        return schematics.stream()
            .filter(schematic -> schematic.locatedAt(path))
            .findAny()
            .orElseThrow();
    }
}
