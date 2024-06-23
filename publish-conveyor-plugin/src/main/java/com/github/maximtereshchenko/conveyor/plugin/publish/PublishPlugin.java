package com.github.maximtereshchenko.conveyor.plugin.publish;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public final class PublishPlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "publish-conveyor-plugin";
    }

    @Override
    public List<ConveyorTaskBinding> bindings(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    ) {
        return List.of(
            new ConveyorTaskBinding(
                Stage.PUBLISH,
                Step.RUN,
                new PublishArtifactTask(
                    Paths.get(configuration.get("artifact.location")),
                    configuration.get("repository"),
                    schematic
                )
            )
        );
    }
}
