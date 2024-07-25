package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.Stage;
import com.github.maximtereshchenko.conveyor.api.TaskCache;
import com.github.maximtereshchenko.conveyor.api.TracingOutputLevel;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.api.port.TracingOutput;

import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public final class ConveyorFacade implements ConveyorModule {

    private final SchematicModelFactory schematicModelFactory;
    private final ClasspathFactory classpathFactory;
    private final PomDefinitionFactory pomDefinitionFactory;
    private final SchematicDefinitionConverter schematicDefinitionConverter;
    private final PreferencesFactory preferencesFactory;
    private final TaskFactory taskFactory;
    private final Executor executor;
    private final Tracer tracer;

    private ConveyorFacade(
        SchematicModelFactory schematicModelFactory,
        ClasspathFactory classpathFactory,
        PomDefinitionFactory pomDefinitionFactory,
        SchematicDefinitionConverter schematicDefinitionConverter,
        PreferencesFactory preferencesFactory,
        TaskFactory taskFactory,
        Executor executor,
        Tracer tracer
    ) {
        this.schematicModelFactory = schematicModelFactory;
        this.classpathFactory = classpathFactory;
        this.pomDefinitionFactory = pomDefinitionFactory;
        this.schematicDefinitionConverter = schematicDefinitionConverter;
        this.preferencesFactory = preferencesFactory;
        this.taskFactory = taskFactory;
        this.executor = executor;
        this.tracer = tracer;
    }

    public static ConveyorModule from(
        SchematicDefinitionConverter schematicDefinitionConverter,
        Executor executor,
        TaskCache taskCache,
        TracingOutput tracingOutput,
        TracingOutputLevel tracingOutputLevel
    ) {
        var tracer = new Tracer(tracingOutput, tracingOutputLevel);
        var cachingSchematicDefinitionConverter = new CachingSchematicDefinitionConverter(
            new TracingSchematicDefinitionConverter(schematicDefinitionConverter, tracer)
        );
        var cachingSchematicModelFactory = new CachingSchematicModelFactory(
            new DefaultSchematicModelFactory(
                cachingSchematicDefinitionConverter,
                tracer
            )
        );
        var taskFactory = new ExecutableTaskFactory();
        return new ConveyorFacade(
            cachingSchematicModelFactory,
            new CachingClasspathFactory(new DefaultClasspathFactory(tracer)),
            PomDefinitionFactory.configured(),
            cachingSchematicDefinitionConverter,
            new PreferencesFactory(cachingSchematicModelFactory),
            switch (taskCache) {
                case ENABLED -> new CacheableTaskFactory(taskFactory);
                case DISABLED -> taskFactory;
            },
            executor,
            tracer
        );
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
                    taskFactory,
                    tracer.withContext(
                        "schematic",
                        extendableLocalInheritanceHierarchyModel.id()
                    )
                )
            )
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return Schematics.from(schematics, initial(schematics, path), executor);
    }

    private Schematic initial(LinkedHashSet<Schematic> schematics, Path path) {
        return schematics.stream()
            .filter(schematic -> schematic.locatedAt(path))
            .findAny()
            .orElseThrow();
    }
}
