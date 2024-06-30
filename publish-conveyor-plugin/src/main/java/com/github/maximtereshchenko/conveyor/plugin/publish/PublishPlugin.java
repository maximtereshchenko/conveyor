package com.github.maximtereshchenko.conveyor.plugin.publish;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PublishPlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "publish-conveyor-plugin";
    }

    @Override
    public List<ConveyorTask> tasks(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    ) {
        return List.of(
            new ConveyorTask(
                "publish-artifact",
                Stage.PUBLISH,
                Step.RUN,
                new PublishArtifactAction(
                    Paths.get(configuration.get("artifact.location"))
                        .toAbsolutePath()
                        .normalize(),
                    configuration.get("repository"),
                    schematic
                ),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            )
        );
    }
}
