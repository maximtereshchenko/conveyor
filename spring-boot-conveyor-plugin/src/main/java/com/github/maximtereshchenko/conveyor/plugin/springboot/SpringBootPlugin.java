package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.springboot.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public final class SpringBootPlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "spring-boot-conveyor-plugin";
    }

    @Override
    public List<ConveyorTaskBinding> bindings(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    ) {
        var containerDirectory = configuredPath(configuration, "container.directory");
        var classpathDirectory = "classpath";
        return List.of(
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.FINALIZE,
                new CopyClassPathTask(
                    configuredPath(configuration, "classes.directory"),
                    containerDirectory.resolve(classpathDirectory),
                    schematic
                )
            ),
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.FINALIZE,
                new ExtractSpringBootLauncherTask(containerDirectory)
            ),
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.FINALIZE,
                new WritePropertiesTask(
                    containerDirectory.resolve(Configuration.PROPERTIES_CLASS_PATH_LOCATION),
                    classpathDirectory,
                    configuration.get("launched.class")
                )
            ),
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.FINALIZE,
                new WriteManifestTask(containerDirectory)
            ),
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.FINALIZE,
                new ArchiveExecutableTask(
                    containerDirectory,
                    configuredPath(configuration, "destination")
                )
            )
        );
    }

    private Path configuredPath(Map<String, String> configuration, String property) {
        return Paths.get(configuration.get(property));
    }
}
