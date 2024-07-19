package com.github.maximtereshchenko.conveyor.plugin.publish;

import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            task(
                "publish-schematic-definition",
                schematic.path(),
                schematic,
                configuration,
                ArtifactClassifier.SCHEMATIC_DEFINITION
            ),
            task(
                "publish-artifact",
                Optional.ofNullable(configuration.get("artifact.location"))
                    .map(Paths::get)
                    .map(Path::toAbsolutePath)
                    .map(Path::normalize)
                    .orElseGet(() ->
                        schematic.path()
                            .getParent()
                            .resolve(".conveyor")
                            .resolve("%s-%s.jar".formatted(schematic.name(), schematic.version()))
                    ),
                schematic,
                configuration,
                ArtifactClassifier.CLASSES
            )
        );
    }

    private ConveyorTask task(
        String name,
        Path path,
        ConveyorSchematic schematic,
        Map<String, String> configuration,
        ArtifactClassifier artifactClassifier
    ) {
        return new ConveyorTask(
            name,
            BindingStage.PUBLISH,
            BindingStep.RUN,
            new PublishArtifactAction(
                path,
                schematic,
                configuration.get("repository"),
                artifactClassifier
            ),
            Set.of(),
            Set.of(),
            Cache.DISABLED
        );
    }
}
