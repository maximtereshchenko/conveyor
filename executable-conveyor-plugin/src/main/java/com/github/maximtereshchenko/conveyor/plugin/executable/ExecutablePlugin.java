package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;

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
        return List.of(
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.FINALIZE,
                new ExtractDependenciesTask(schematic)
            ),
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.FINALIZE,
                new WriteManifestTask(schematic, configuration.get("main-class"))
            ),
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.FINALIZE,
                new ArchiveExecutableTask(
                    schematic,
                    schematic.constructionDirectory()
                        .resolve(
                            "%s-%s-executable.jar".formatted(
                                schematic.coordinates().name(),
                                schematic.coordinates().version()
                            )
                        )
                )
            )
        );
    }
}
