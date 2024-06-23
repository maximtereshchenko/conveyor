package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public final class ExecutablePlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "executable-conveyor-plugin";
    }

    @Override
    public List<ConveyorTaskBinding> bindings(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    ) {
        var classesDirectory = configuredPath(configuration, "classes.directory");
        return List.of(
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.FINALIZE,
                new ExtractDependenciesTask(schematic, classesDirectory)
            ),
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.FINALIZE,
                new WriteManifestTask(classesDirectory, configuration.get("main.class"))
            ),
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.FINALIZE,
                new ArchiveExecutableTask(
                    classesDirectory,
                    configuredPath(configuration, "destination")
                )
            )
        );
    }

    private Path configuredPath(Map<String, String> configuration, String property) {
        return Paths.get(configuration.get(property));
    }
}
