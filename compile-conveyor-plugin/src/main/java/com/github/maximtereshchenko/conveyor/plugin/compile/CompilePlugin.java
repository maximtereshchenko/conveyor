package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CompilePlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "compile-conveyor-plugin";
    }

    @Override
    public List<ConveyorTask> tasks(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    ) {
        var sourcesDirectory = configuredPath(configuration, "sources.directory");
        var classesDirectory = configuredPath(configuration, "classes.directory");
        var testSourcesDirectory = configuredPath(configuration, "test.sources.directory");
        var testClassesDirectory = configuredPath(configuration, "test.classes.directory");
        var compiler = new Compiler();
        return List.of(
            new ConveyorTask(
                "compile-sources",
                Stage.COMPILE,
                Step.RUN,
                new CompileSourcesAction(
                    sourcesDirectory,
                    classesDirectory,
                    compiler,
                    schematic
                ),
                Set.of(sourcesDirectory),
                Set.of(classesDirectory),
                Cache.ENABLED
            ),
            new ConveyorTask(
                "publish-exploded-jar-artifact",
                Stage.COMPILE,
                Step.FINALIZE,
                new PublishExplodedJarArtifactTask(classesDirectory),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            ),
            new ConveyorTask(
                "compile-test-sources",
                Stage.TEST,
                Step.PREPARE,
                new CompileTestSourcesAction(
                    testSourcesDirectory,
                    testClassesDirectory,
                    compiler,
                    schematic,
                    classesDirectory
                ),
                Set.of(classesDirectory, testSourcesDirectory),
                Set.of(testClassesDirectory),
                Cache.ENABLED
            )
        );
    }

    private Path configuredPath(Map<String, String> configuration, String property) {
        return Paths.get(configuration.get(property)).toAbsolutePath().normalize();
    }
}
