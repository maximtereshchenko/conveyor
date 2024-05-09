package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.springboot.Configuration;

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
        var executableContainer = schematic.constructionDirectory()
            .resolve("executable-container");
        var classPathDirectory = "class-path";
        return List.of(
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.FINALIZE,
                new CopyClassPathTask(
                    schematic,
                    executableContainer.resolve(classPathDirectory)
                )
            ),
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.FINALIZE,
                new ExtractSpringBootLauncherTask(schematic, executableContainer)
            ),
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.FINALIZE,
                new WritePropertiesTask(
                    schematic,
                    executableContainer.resolve(
                        Configuration.PROPERTIES_CLASS_PATH_LOCATION
                    ),
                    classPathDirectory,
                    configuration.get("launched-class")
                )
            ),
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.FINALIZE,
                new WriteManifestTask(schematic, executableContainer)
            ),
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.FINALIZE,
                new ArchiveExecutableTask(schematic, executableContainer)
            )
        );
    }
}
