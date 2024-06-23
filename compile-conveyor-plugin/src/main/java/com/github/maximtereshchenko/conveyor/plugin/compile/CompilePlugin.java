package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public final class CompilePlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "compile-conveyor-plugin";
    }

    @Override
    public List<ConveyorTaskBinding> bindings(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    ) {
        var classesDirectory = configuredPath(configuration, "classes.directory");
        var compiler = new Compiler();
        return List.of(
            new ConveyorTaskBinding(
                Stage.COMPILE,
                Step.RUN,
                new CompileSourcesTask(
                    configuredPath(configuration, "sources.directory"),
                    classesDirectory,
                    compiler,
                    schematic
                )
            ),
            new ConveyorTaskBinding(
                Stage.COMPILE,
                Step.FINALIZE,
                new PublishExplodedJarArtifactTask(classesDirectory)
            ),
            new ConveyorTaskBinding(
                Stage.TEST,
                Step.PREPARE,
                new CompileTestSourcesTask(
                    configuredPath(configuration, "test.sources.directory"),
                    configuredPath(configuration, "test.classes.directory"),
                    compiler,
                    schematic,
                    classesDirectory
                )
            )
        );
    }

    private Path configuredPath(Map<String, String> configuration, String property) {
        return Paths.get(configuration.get(property));
    }
}
